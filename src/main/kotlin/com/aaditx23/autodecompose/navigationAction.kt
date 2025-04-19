package com.aaditx23.autodecompose

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.ui.Messages

class NavigationAction : AnAction("Add Decompose Navigation") {
    override fun actionPerformed(e: AnActionEvent) {
        val dialog = NavigationDialog()
        if (dialog.showAndGet()) {
            val composableName = dialog.composableNameField.text.trim()
            val childPath = dialog.childFileChooser.text
            val configPath = dialog.configFileChooser.text
            val navRootPath = dialog.navRootFileChooser.text
            val rootComponentPath = dialog.rootComponentFileChooser.text

            ProgressManager.getInstance().runProcessWithProgressSynchronously(
                {
                    ApplicationManager.getApplication().runWriteAction {
                        // TODO: Replace this with real PSI read/write logic
                        Messages.showInfoMessage(
                            """
                            Composable: $composableName
                            Child: $childPath
                            Config: $configPath
                            NavRoot: $navRootPath
                            RootComponent: $rootComponentPath
                            """.trimIndent(),
                            "Captured Inputs"
                        )
                    }
                },
                "Generating Decompose Navigation",
                false, // cancellable
                e.project
            )
        }
    }
}
