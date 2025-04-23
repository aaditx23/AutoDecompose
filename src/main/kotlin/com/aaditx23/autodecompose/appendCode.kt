package com.aaditx23.autodecompose

import com.aaditx23.autodecompose.TemplateProvider.navRootEntry
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.*
import com.intellij.openapi.project.Project

import com.intellij.psi.PsiFile

import com.intellij.openapi.command.WriteCommandAction

fun appendToNavRootFile(
    navRootFilePath: String,
    composableName: String,
    project: Project,
    componentPackage: String
) {
    val psiFile = findPsiFile(navRootFilePath, project) ?: return
    val document = FileDocumentManager.getInstance().getDocument(psiFile.virtualFile) ?: return

    val originalText = document.text
    val (newStatement, importLine) = navRootEntry(composableName, componentPackage)

    ApplicationManager.getApplication().runWriteAction {
        WriteCommandAction.runWriteCommandAction(project) {
            var updatedText = document.text

            // 1. Insert new child statement after the last "is Child.Xyz ->"
            val childRegex = Regex("""(is\s+Child\.\w+\s+->\s+.+)""")
            val childMatches = childRegex.findAll(updatedText).toList()

            if (childMatches.isNotEmpty()) {
                val lastMatch = childMatches.last()
                val lastLine = updatedText.substring(0, lastMatch.range.last + 1)
                val remaining = updatedText.substring(lastMatch.range.last + 1)


                // Insert the statement as a new line after the match
                updatedText = "$lastLine\n                        $newStatement$remaining"
            }

            // 2. Insert the import 2 lines above the function definition
            val importRegex = Regex("""fun\s+NavigationRoot\s*\(""")
            val navRootMatch = importRegex.find(updatedText)
            if (navRootMatch != null) {
                val lines = updatedText.lines().toMutableList()
                val navRootLineIndex = lines.indexOfFirst { it.contains(navRootMatch.value) }
                val importInsertIndex = (navRootLineIndex - 2).coerceAtLeast(0)
                lines.add(importInsertIndex, importLine)
                updatedText = lines.joinToString("\n")
            }

            // Finally, apply updated text
            document.setText(updatedText)
            FileDocumentManager.getInstance().saveDocument(document)
        }
    }
}

fun appendRootComponent(
    rootComponentPath: String,
    composableName: String,
    project: Project,
    childFunPackage: String
) {
    val psiFile = findPsiFile(rootComponentPath, project) ?: return
    val document = FileDocumentManager.getInstance().getDocument(psiFile.virtualFile) ?: return
    val fileLines = document.text.lines().toMutableList()

    // Get root component entry (statement and import)
    val (newConfigLine, importLine) = TemplateProvider.rootComponentEntry(composableName, childFunPackage)

    ApplicationManager.getApplication().runWriteAction {
        WriteCommandAction.runWriteCommandAction(project) {

            // 1. Insert new Configuration entry after the last Configuration.XConfig -> ... line
            val configRegex = Regex("""Configuration\.\w+Config\s*->\s*\w+Child\(.*\)""")
            val lastConfigLineIndex = fileLines.indexOfLast { configRegex.containsMatchIn(it) }
            if (lastConfigLineIndex != -1) {
                fileLines.add(lastConfigLineIndex + 1, newConfigLine)
            }

            // 2. Insert import above class RootComponent
            val classIndex = fileLines.indexOfFirst { it.contains("class RootComponent(") }
            if (classIndex != -1) {
                fileLines.add(classIndex, importLine)
            }

            // Apply changes
            document.setText(fileLines.joinToString("\n"))
            FileDocumentManager.getInstance().saveDocument(document)
        }
    }
}




fun appendToChildFile(childFilePath: String, composableName: String, project: Project, compPackage: String) {
    val psiFile = findPsiFile(childFilePath, project) ?: return
    val document = FileDocumentManager.getInstance().getDocument(psiFile.virtualFile) ?: return

    val fileText = document.text

    // Regex to find the last data class entry (i.e., Child.XYZ)
    val dataClassRegex = Regex("""data class\s+\w+Child\(.*\):\s+Child\(\)""")
    val matches = dataClassRegex.findAll(fileText).toList()
    if (matches.isEmpty()) return

    val lastMatch = matches.last()
    val insertOffset = lastMatch.range.last + 1 // insert after the match line

    val (newChildEntry, importLine) = TemplateProvider.childEntry(composableName, compPackage)

    // Regex to find where to insert the import (before `sealed class Child`)
    val sealedClassRegex = Regex("""sealed\s+class\s+Child\s*[{(]""")
    val sealedMatch = sealedClassRegex.find(fileText)
    val importInsertOffset = sealedMatch?.range?.first ?: return

    ApplicationManager.getApplication().runWriteAction {
        WriteCommandAction.runWriteCommandAction(project) {
            // Then insert the new child entry
            document.insertString(insertOffset, "\n    $newChildEntry")
            // Insert the import line first
            document.insertString(importInsertOffset, "$importLine\n\n")
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
