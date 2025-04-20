package com.aaditx23.autodecompose

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiManager
import java.io.File
import javax.swing.JTextField

class NavigationAction : AnAction("Add Decompose Navigation") {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val dialog = NavigationDialog(project)

        // Save immediately on browse selection
        fun setupChangeListener(chooser: JTextField) {
            chooser.document.addDocumentListener(object : javax.swing.event.DocumentListener {
                override fun insertUpdate(e: javax.swing.event.DocumentEvent?) = save()
                override fun removeUpdate(e: javax.swing.event.DocumentEvent?) = save()
                override fun changedUpdate(e: javax.swing.event.DocumentEvent?) = save()
                private fun save() {
                    PathStorage.save(project, StoredPaths(
                        childFile = dialog.childFileChooser.text,
                        configFile = dialog.configFileChooser.text,
                        navRootFile = dialog.navRootFileChooser.text,
                        rootComponentFile = dialog.rootComponentFileChooser.text,
                        childFunctionDir = dialog.childFunctionDirChooser.text,
                        composableFileDir = dialog.composableFileDirChooser.text
                    ))
                }
            })
        }

        listOf(
            dialog.childFileChooser.textField,
            dialog.configFileChooser.textField,
            dialog.navRootFileChooser.textField,
            dialog.rootComponentFileChooser.textField,
            dialog.childFunctionDirChooser.textField,
            dialog.composableFileDirChooser.textField
        ).forEach { setupChangeListener(it) }

        if (dialog.showAndGet()) {
            val composableName = dialog.composableNameField.text.trim()
            val childPath = dialog.childFileChooser.text.trim()
            val configPath = dialog.configFileChooser.text.trim()
            val navRootPath = dialog.navRootFileChooser.text.trim()
            val rootComponentPath = dialog.rootComponentFileChooser.text.trim()
            val childFunctionDir = dialog.childFunctionDirChooser.text.trim()
            val composableFileDir = dialog.composableFileDirChooser.text.trim()

            ProgressManager.getInstance().runProcessWithProgressSynchronously(
                {
                    ApplicationManager.getApplication().invokeAndWait {
                        // 👉 Append to Navigation Root
                        appendToNavRootFile(navRootPath, composableName, project)
                        appendToChildFile(childPath, composableName, project)
                        appendToConfigFile(configPath, composableName, project)

                        // 👉 Create Child Function File
                        val childFuncFileName = "${composableName.lowerFirst()}Child.kt"
                        createKotlinFileInDirIfPossible(project, childFunctionDir, childFuncFileName, TemplateProvider.childFunctionTemplate(
                            composableName,
                            getPackageName(project, childPath) ?: "",
                            getPackageName(project, configPath) ?: "",
                            getPackageName(project, childFunctionDir) ?: ""
                        ))

                        // 👉 Create Composable Function File
                        val composableFileName = "${composableName}Screen.kt"
                        createKotlinFileInDirIfPossible(project, composableFileDir, composableFileName, TemplateProvider.composableTemplate(
                            composableName,
                            getPackageName(project, composableFileDir) ?: ""
                        ))

                        // 👉 Create Component File
                        val componentFileName = "${composableName}Component.kt"
                        createKotlinFileInDirIfPossible(project, composableFileDir, componentFileName, TemplateProvider.componentTemplate(
                            composableName,
                            getPackageName(project, composableFileDir) ?: ""
                        ))

                        // 👉 Create Event File
                        val eventFileName = "${composableName}Event.kt"
                        createKotlinFileInDirIfPossible(project, composableFileDir, eventFileName, TemplateProvider.eventTemplate(
                            composableName,
                            getPackageName(project, composableFileDir) ?: ""
                        ))
                    }
                },
                "Generating Decompose Navigation",
                false,
                project
            )
        }
    }

    private fun createKotlinFileIfPossible(
        project: com.intellij.openapi.project.Project,
        filePath: String,
        content: String
    ) {
        val dirFile = File(filePath).parentFile
        val dirVfs = VfsUtil.findFileByIoFile(dirFile, true)
        val psiDir = dirVfs?.let { PsiManager.getInstance(project).findDirectory(it) }

        psiDir?.let {
            createFile(
                filePath = filePath,
                directory = it,
                fileName = File(filePath).name,
                content = content,
                project = project
            )
        }
    }

    private fun createKotlinFileInDirIfPossible(
        project: com.intellij.openapi.project.Project,
        dirPath: String,
        fileName: String,
        content: String
    ) {
        val dirFile = File(dirPath)
        val dirVfs = VfsUtil.findFileByIoFile(dirFile, true)
        val psiDir = dirVfs?.let { PsiManager.getInstance(project).findDirectory(it) }

        psiDir?.let {
            createFile(
                filePath = "$dirPath/$fileName",
                directory = it,
                fileName = fileName,
                content = content,
                project = project
            )
        }
    }

    private fun getPackageName(project: com.intellij.openapi.project.Project, filePath: String): String? {
        val virtualFile = VfsUtil.findFileByIoFile(File(filePath), true)
        val psiFile = virtualFile?.let { PsiManager.getInstance(project).findFile(it) }
        return PackageProvider.fromPsiFile(psiFile)
    }

}

fun String.lowerFirst(): String = replaceFirstChar { it.lowercaseChar() }
