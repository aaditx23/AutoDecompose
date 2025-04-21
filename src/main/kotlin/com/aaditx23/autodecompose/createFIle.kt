package com.aaditx23.autodecompose

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.psi.KtPsiFactory


fun createFile(filePath: String, directory: PsiDirectory, fileName: String, content: String, project: Project): PsiFile? {
    // Check if the file already exists
    val file = directory.findFile(fileName)
    if (file != null) return file

    // Create the file content using KtPsiFactory
    val ktPsiFactory = KtPsiFactory(project)
    val ktFile = ktPsiFactory.createFile(fileName, content)

    // Wrap the file creation inside a WriteCommandAction to ensure it's done on the correct thread
    return WriteCommandAction.writeCommandAction(project).compute<PsiFile?, Throwable> {
        try {
            // Add the file to the directory
            val addedFile = directory.add(ktFile) as? PsiFile
            addedFile?.let {
                // Ensure the document is saved
                it.virtualFile?.let { virtualFile ->
                    FileDocumentManager.getInstance().getDocument(virtualFile)?.let { doc ->
                        FileDocumentManager.getInstance().saveDocument(doc)
                    }
                }
            }
            addedFile
        } catch (e: Exception) {
            // Log any error that might occur during the file creation
            ApplicationManager.getApplication().invokeLater {
                // If there's an error, you can log or show it to the user here
                e.printStackTrace() // Consider replacing this with proper logging
            }
            null
        }
    }
}
