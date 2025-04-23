package com.aaditx23.autodecompose

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiManager
import java.io.File
import javax.swing.SwingUtilities


// will be used if necessary later

class DecomposeNavigationGenerator(
    private val project: Project,
    private val dialog: NavigationDialog
) {

    private val flags = mutableMapOf(
        "navRoot" to false,
        "rootComp" to false,
        "child" to false,
        "config" to false,
        "function" to false,
        "composables" to false
    )

    private var pkg = Packages("", "", "", false)
    private var psi = PsiDirs()

    // Update status function
    private fun updateStatus() {
        val status = buildString {
            appendLine("Status:")
            appendLine("✔ NavRoot Updated: ${flags["navRoot"]}")
            appendLine("✔ RootComponent Updated: ${flags["rootComp"]}")
            appendLine("✔ Child.kt Appended: ${flags["child"]}")
            appendLine("✔ Config.kt Appended: ${flags["config"]}")
            appendLine("✔ Child Function Created: ${flags["function"]}")
            appendLine("✔ Composable Files Created: ${flags["composables"]}")
        }
        SwingUtilities.invokeLater {
            dialog.statusLabel.text = "<html>${status.replace("\n", "<br>")}</html>"
        }
    }

    // Helper function to extract and set packages
    private fun setPackages() {
        val composable = dialog.composableNameField.text.trim()

        val projectBase = project.basePath ?: return

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
    }

    // Function to update NavRoot
    private fun updateNavRoot() {
        ApplicationManager.getApplication().invokeAndWait {
            appendToNavRootFile(dialog.navRootFileChooser.text, dialog.composableNameField.text.trim(), project, pkg.composable)
            flags["navRoot"] = true
            updateStatus()
        }
    }

    // Function to append to Child.kt
    private fun appendChildFile() {
        ApplicationManager.getApplication().invokeAndWait {
            appendToChildFile(dialog.childFileChooser.text, dialog.composableNameField.text.trim(), project, pkg.composable)
            flags["child"] = true
            updateStatus()
        }
    }

    // Function to append RootComponent
    private fun appendRootComponent() {
        ApplicationManager.getApplication().invokeAndWait {
            appendRootComponent(dialog.rootComponentFileChooser.text, dialog.composableNameField.text.trim(), project, pkg.childFun)
            flags["rootComp"] = true
            updateStatus()
        }
    }

    // Function to append to Config.kt
    private fun appendConfigFile() {
        ApplicationManager.getApplication().invokeAndWait {
            appendToConfigFile(dialog.configFileChooser.text, dialog.composableNameField.text.trim(), project)
            flags["config"] = true
            updateStatus()
        }
    }

    // Function to create Child Function
    private fun createChildFunction() {
        ApplicationManager.getApplication().invokeAndWait {
            val dir = File(dialog.childFunctionDirChooser.text)
            val vf = VfsUtil.findFileByIoFile(dir, true)
            val psiDir = vf?.let { PsiManager.getInstance(project).findDirectory(it) }

            val childCode = TemplateProvider.childFunctionTemplate(dialog.composableNameField.text.trim(), pkg)

            psiDir?.let {
                createFile(
                    filePath = "${dir.path}/${dialog.composableNameField.text.trim().unCapitalize()}Child.kt",
                    directory = it,
                    fileName = "${dialog.composableNameField.text.trim().unCapitalize()}Child.kt",
                    content = childCode,
                    project = project
                )
            }

            flags["function"] = true
            updateStatus()
        }
    }

    // Function to create Composable, Component, Event files
    private fun createComposableFiles() {
        ApplicationManager.getApplication().invokeAndWait {
            val dirComposable = File(dialog.composableFilesDirChooser.text)
            val vfComposable = VfsUtil.findFileByIoFile(dirComposable, true)
            val psiDirComposable = vfComposable?.let { PsiManager.getInstance(project).findDirectory(it) }
            val composablePackage = PackageProvider.fromPsiDirectory(psiDirComposable) ?: return@invokeAndWait

            val files = listOf(
                "${dialog.composableNameField.text.trim()}.kt" to TemplateProvider.composableTemplate(dialog.composableNameField.text.trim(), composablePackage),
                "${dialog.composableNameField.text.trim()}Component.kt" to TemplateProvider.componentTemplate(dialog.composableNameField.text.trim(), composablePackage),
                "${dialog.composableNameField.text.trim()}Event.kt" to TemplateProvider.eventTemplate(dialog.composableNameField.text.trim(), composablePackage)
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

    // Main action function
    fun execute() {
        ProgressManager.getInstance().runProcessWithProgressSynchronously({
            // Step 1: Set up packages and psi dirs
            setPackages()

            // Step 2: Perform all tasks sequentially
            updateNavRoot()
            appendChildFile()
            appendRootComponent()
            appendConfigFile()
            createChildFunction()
            createComposableFiles()

            // Wait for all tasks to complete
            if (flags.values.all { it }) {
                Thread.sleep(500) // Slight delay before closing
                SwingUtilities.invokeLater {
//                    dialog.close(0)
                }
            }

        }, "Generating Navigation...", false, project)
    }
}
