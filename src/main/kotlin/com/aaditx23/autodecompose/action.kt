package com.aaditx23.autodecompose

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiManager
import java.io.File
import javax.swing.SwingUtilities


data class Packages(
    var composable: String,
    var childFun: String,
    var config: String,
    var flag: Boolean
){
    fun print(){
        println("composable: $composable")
        println("child: $childFun")
        println("config: $config")
    }
}

data class PsiDirs(
    var childFunction: PsiDirectory? = null,
    var composable: PsiDirectory? = null
){
    fun print(){
        println("ChildFunction: ${childFunction.toString()}")
        println("Composable: ${composable.toString()}")
    }
}

fun action(project: Project, dialog: NavigationDialog) {


    val flags = mutableMapOf(
        "navRoot" to false,
        "child" to false,
        "config" to false,
        "function" to false,
        "composables" to false
    )
    var packageFlag = false
    var pkg = Packages("", "", "", false)
    var psi = PsiDirs()


    fun updateStatus() {
        val status = buildString {
            appendLine("Status:")
            appendLine("✔ NavRoot Updated: ${flags["navRoot"]}")
            appendLine("✔ Child.kt Appended: ${flags["child"]}")
            appendLine("✔ Config.kt Appended: ${flags["config"]}")
            appendLine("✔ Child Function Created: ${flags["function"]}")
            appendLine("✔ Composable Files Created: ${flags["composables"]}")
        }
        SwingUtilities.invokeLater {
            dialog.statusLabel.text = "<html>${status.replace("\n", "<br>")}</html>"
        }
    }

    ProgressManager.getInstance().runProcessWithProgressSynchronously(
        {


            val composable = dialog.composableNameField.text.trim()

            val projectBase = project.basePath ?: return@runProcessWithProgressSynchronously

            ApplicationManager.getApplication().invokeAndWait {
                val dirComposable = File(dialog.composableFilesDirChooser.text)
                val vfComposable = VfsUtil.findFileByIoFile(dirComposable, true)
                val psiDirComposable = vfComposable?.let { PsiManager.getInstance(project).findDirectory(it) }
                val compPkg = PackageProvider.fromPsiDirectory(psiDirComposable)      // WAS STUCK HERE
                if (compPkg == null) {
                    println("❌ compPkg is null for path: ${dialog.composableFilesDirChooser.text}")
                    return@invokeAndWait
                }


                val dirChildFunction = File(dialog.childFunctionDirChooser.text)
                val vfChildFunction = VfsUtil.findFileByIoFile(dirChildFunction, true)
                val psiDirChildFunction = vfChildFunction?.let { PsiManager.getInstance(project).findDirectory(it) }
                val childPkg = PackageProvider.fromPsiDirectory(psiDirChildFunction)
                if (childPkg == null) {
                    println("❌ childPkg is null")
                    return@invokeAndWait
                }

                val configPkg = PackageProvider.fromPath(project, dialog.configFileChooser.text)
                if (configPkg == null) {
                    println("❌ configPkg is null for path: ${dialog.configFileChooser.text}")
                    return@invokeAndWait
                }
                pkg = Packages(
                    composable = compPkg,
                    childFun = childPkg,
                    config = configPkg,
                    flag = true
                )
                psi.childFunction = psiDirChildFunction
                psi.composable = psiDirComposable
                psi.print()

                pkg.print()


            }

            // Task 1: Update NavRoot
            if(pkg.flag){
                ApplicationManager.getApplication().invokeAndWait {
                    appendToNavRootFile(dialog.navRootFileChooser.text, composable, project, pkg.composable)
                    flags["navRoot"] = true
                    updateStatus()
                }
            }

            if (flags["navRoot"] == true) {
                // Task 2: Append to Child.kt
                ApplicationManager.getApplication().invokeAndWait {
                    appendToChildFile(dialog.childFileChooser.text, composable, project, pkg.composable)
                    flags["child"] = true
                    updateStatus()
                }
            }

            if (flags["child"] == true) {
                // Task 3: Append to Config.kt
                ApplicationManager.getApplication().invokeAndWait {
                    appendToConfigFile(dialog.configFileChooser.text, composable, project)
                    flags["config"] = true
                    updateStatus()
                }
            }

            if (flags["config"] == true) {
                // Task 4: Create Child Function File
                ApplicationManager.getApplication().invokeAndWait {
                    val dir = File(dialog.childFunctionDirChooser.text)
                    val vf = VfsUtil.findFileByIoFile(dir, true)
                    val psiDir = vf?.let { PsiManager.getInstance(project).findDirectory(it) }
                    println("PSIDIR for Child Function: ${psiDir.toString()}")

                    val childCode = TemplateProvider.childFunctionTemplate(composable, pkg)

                    psiDir?.let {
                        createFile(
                            filePath = "${dir.path}/${composable}Child.kt",
                            directory = it,
                            fileName = "${composable}Child.kt",
                            content = childCode,
                            project = project
                        )
                    }

                    flags["function"] = true
                    updateStatus()
                }
            }

            if (flags["function"] == true) {
                // Task 5: Create Composable, Component, Event
                ApplicationManager.getApplication().invokeAndWait {
                    val dirComposable = File(dialog.composableFilesDirChooser.text)
                    val vfComposable = VfsUtil.findFileByIoFile(dirComposable, true)
                    val psiDirComposable = vfComposable?.let { PsiManager.getInstance(project).findDirectory(it) }
                    val composablePackage = PackageProvider.fromPsiDirectory(psiDirComposable) ?: return@invokeAndWait
                    println("PSIDIR for COMPOSABLES: ${psiDirComposable.toString()}")

                    println("PACKAGES for COMPOSABLES: ${composablePackage}\n")

                    val files = listOf(
                        "${composable}.kt" to TemplateProvider.composableTemplate(composable, composablePackage),
                        "${composable}Component.kt" to TemplateProvider.componentTemplate(composable, composablePackage),
                        "${composable}Event.kt" to TemplateProvider.eventTemplate(composable, composablePackage)
                    )

                    files.forEach { (name, content) ->
                        psiDirComposable?.let {
                            createFile(
                                filePath = "${dirComposable.path}/$name",
                                directory = it,
                                fileName = name,
                                content = content,
                                project = project
                            )
                        }
                    }

                    flags["composables"] = true
                    updateStatus()
                }
            }

            if (flags.values.all { it }) {
                Thread.sleep(500) // Slight delay before closing
                SwingUtilities.invokeLater {
//                    dialog.close(0)
                }
            }

        },
        "Generating Navigation...",
        false,
        project
    )
}

