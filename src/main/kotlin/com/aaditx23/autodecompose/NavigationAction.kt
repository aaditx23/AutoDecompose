package com.aaditx23.autodecompose

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiManager
import java.io.File
import javax.swing.SwingUtilities

class NavigationAction : AnAction("Add Decompose Navigation") {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val dialog = NavigationDialog(project)

        if (!dialog.showAndGet()) return


    }
}
