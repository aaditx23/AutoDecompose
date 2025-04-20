package com.aaditx23.autodecompose

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.psi.KtPsiFactory


fun createFile(filePath: String, directory: PsiDirectory, fileName: String, content: String, project: Project): PsiFile? {
    val file = directory.findFile(fileName)
    if (file != null) return file

    val ktPsiFactory = KtPsiFactory(project)
    val ktFile = ktPsiFactory.createFile(fileName, content)

    return WriteCommandAction.writeCommandAction(project).compute<PsiFile?, Throwable> {
        val addedFile = directory.add(ktFile) as? PsiFile
        addedFile?.virtualFile?.let {
            FileDocumentManager.getInstance().getDocument(it)?.let { doc ->
                FileDocumentManager.getInstance().saveDocument(doc)
            }
        }
        addedFile
    }
}
