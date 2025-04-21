package com.aaditx23.autodecompose

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiManager
import java.io.File
import javax.swing.SwingUtilities


fun action(project: Project, dialog: NavigationDialog) {


    val flags = mutableMapOf(
        "navRoot" to false,
        "child" to false,
        "config" to false,
        "function" to false,
        "composables" to false
    )

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

            // Task 1: Update NavRoot
            ApplicationManager.getApplication().invokeAndWait {
                appendToNavRootFile(dialog.navRootFileChooser.text, composable, project)
                flags["navRoot"] = true
                updateStatus()
            }

            if (flags["navRoot"] == true) {
                // Task 2: Append to Child.kt
                ApplicationManager.getApplication().invokeAndWait {
                    appendToChildFile(dialog.childFileChooser.text, composable, project)
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

                    val childPkg = PackageProvider.fromPsiDirectory(psiDir) ?: return@invokeAndWait
                    val compPkg = PackageProvider.fromPath(project, dialog.rootComponentFileChooser.text) ?: return@invokeAndWait
                    val configPkg = PackageProvider.fromPath(project, dialog.configFileChooser.text) ?: return@invokeAndWait

                    val childCode = TemplateProvider.childFunctionTemplate(composable, compPkg, configPkg, childPkg)

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
                    val dir = File(dialog.composableFilesDirChooser.text)
                    val vf = VfsUtil.findFileByIoFile(dir, true)
                    val psiDir = vf?.let { PsiManager.getInstance(project).findDirectory(it) }
                    val pkg = PackageProvider.fromPsiDirectory(psiDir) ?: return@invokeAndWait

                    val files = listOf(
                        "${composable}.kt" to TemplateProvider.composableTemplate(composable, pkg),
                        "${composable}Component.kt" to TemplateProvider.componentTemplate(composable, pkg),
                        "${composable}Event.kt" to TemplateProvider.eventTemplate(composable, pkg)
                    )

                    files.forEach { (name, content) ->
                        psiDir?.let {
                            createFile(
                                filePath = "${dir.path}/$name",
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
                    dialog.close(0)
                }
            }

        },
        "Generating Navigation...",
        false,
        project
    )
}

