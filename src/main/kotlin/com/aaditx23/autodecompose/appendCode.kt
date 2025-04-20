package com.aaditx23.autodecompose

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.IncorrectOperationException
import com.intellij.openapi.project.Project

import com.intellij.psi.PsiFile

import com.intellij.openapi.command.WriteCommandAction

fun appendToNavRootFile(navRootFilePath: String, composableName: String, project: Project) {
    val psiFile = findPsiFile(navRootFilePath, project) ?: return
    val document = FileDocumentManager.getInstance().getDocument(psiFile.virtualFile) ?: return

    val fileText = document.text

    // Regex to find the last `is Child.XYZ -> Something(...)` line
    val regex = Regex("""(is\s+Child\.\w+\s+->\s+.*)""")
    val matches = regex.findAll(fileText).toList()
    if (matches.isEmpty()) return

    val lastMatch = matches.last()
    val insertOffset = lastMatch.range.last + 1 // insert after the match line

    val newChildEntry = "is Child.${composableName}Child -> ${composableName}Screen(instance.component)"

    // Wrap document modification in a write action
    ApplicationManager.getApplication().runWriteAction {
        WriteCommandAction.runWriteCommandAction(project) {
            document.insertString(insertOffset, "\n                        $newChildEntry")
            FileDocumentManager.getInstance().saveDocument(document)
        }
    }
}
fun appendToChildFile(childFilePath: String, composableName: String, project: Project) {
    val psiFile = findPsiFile(childFilePath, project) ?: return
    val document = FileDocumentManager.getInstance().getDocument(psiFile.virtualFile) ?: return

    val fileText = document.text

    // Regex to find the last data class entry (i.e., Child.XYZ)
    val regex = Regex("""data class\s+\w+Child\(.*\):\s+Child\(\)""")
    val matches = regex.findAll(fileText).toList()
    if (matches.isEmpty()) return

    val lastMatch = matches.last()
    val insertOffset = lastMatch.range.last + 1 // insert after the match line

    val newChildEntry = TemplateProvider.childEntry(composableName)

    // Wrap document modification in a write action
    ApplicationManager.getApplication().runWriteAction {
        WriteCommandAction.runWriteCommandAction(project) {
            document.insertString(insertOffset, "\n    $newChildEntry")
            FileDocumentManager.getInstance().saveDocument(document)
        }
    }
}

fun appendToConfigFile(configFilePath: String, composableName: String, project: Project) {
    val psiFile = findPsiFile(configFilePath, project) ?: return
    val document = FileDocumentManager.getInstance().getDocument(psiFile.virtualFile) ?: return

    val fileText = document.text

    // Regex to find the last Configuration entry
    val regex = Regex("""sealed\s+class\s+Configuration""")
    val matches = regex.findAll(fileText).toList()
    if (matches.isEmpty()) return

    val lastMatch = matches.last()
    val insertOffset = lastMatch.range.last + 3 // insert after the match line

    val newConfigEntry = TemplateProvider.configurationEntry(composableName)

    // Wrap document modification in a write action
    ApplicationManager.getApplication().runWriteAction {
        WriteCommandAction.runWriteCommandAction(project) {
            document.insertString(insertOffset, "\n    $newConfigEntry")
            FileDocumentManager.getInstance().saveDocument(document)
        }
    }
}


fun findPsiFile(filePath: String, project: Project): PsiFile? {
    // Get the virtual file and create a PsiFile from it
    val virtualFile = VfsUtil.findFileByIoFile(java.io.File(filePath), true) ?: return null
    return PsiManager.getInstance(project).findFile(virtualFile)
}


fun findChildrenBlock(navRootFile: PsiFile): PsiElement? {
    // Search for the "Children" block in the NavRoot file (implement your search logic)
    val fileText = navRootFile.text
    val childrenBlockStart = fileText.indexOf("Children(")
    if (childrenBlockStart == -1) return null

    val childrenBlockEnd = fileText.indexOf(")", childrenBlockStart)
    if (childrenBlockEnd == -1) return null

    // Return the block (the element within "Children(...)")
    return navRootFile.findElementAt(childrenBlockStart)
}
