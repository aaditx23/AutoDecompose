package com.aaditx23.autodecompose

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import java.awt.BorderLayout
import java.awt.Dimension
import java.io.File
import javax.swing.*
import kotlin.io.path.Path
import kotlin.io.path.exists

class NavigationDialog(val project: Project) : DialogWrapper(true) {

    companion object {
        private var instance: NavigationDialog? = null

        fun getInstance(project: Project): NavigationDialog {
            return instance ?: NavigationDialog(project).also {
                instance = it
            }
        }
    }

    init {
        instance = this
    }

    val composableNameField = JTextField(20)
    val childFileChooser = FileChooserWithButton("Child.kt")
    val configFileChooser = FileChooserWithButton("Config.kt")
    val navRootFileChooser = FileChooserWithButton("NavRoot.kt")
    val rootComponentFileChooser = FileChooserWithButton("RootComponent.kt")
    val childFunctionDirChooser = FileChooserWithButton("ChildFunctionDir")
    val composableFilesDirChooser = FileChooserWithButton("ComposableDir")

    val statusLabel = JLabel("Ready")
    val triggerActionButton = JButton("Run")

    init {
        title = "Add Decompose Navigation"
        init()
        loadDefaults()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            preferredSize = Dimension(1000, 400)
        }

        panel.add(JLabel("Composable Name:"))
        panel.add(composableNameField)

        panel.add(JLabel("Composable Files Directory:"))
        panel.add(composableFilesDirChooser)

        panel.add(JLabel("Child File:"))
        panel.add(childFileChooser)

        panel.add(JLabel("Config File:"))
        panel.add(configFileChooser)

        panel.add(JLabel("NavRoot File:"))
        panel.add(navRootFileChooser)

        panel.add(JLabel("Root Component File:"))
        panel.add(rootComponentFileChooser)

        panel.add(JLabel("Child Function Directory:"))
        panel.add(childFunctionDirChooser)

        panel.add(Box.createVerticalStrut(10))
        panel.add(statusLabel)

        // Add "Trigger Action" button
        panel.add(triggerActionButton)

        // Add action listener to the button
        triggerActionButton.addActionListener {
            action(project, instance!!)
        }

        return panel
    }

    private fun loadDefaults() {
        val base = File(project.basePath ?: return)
        val composeRoot = File(base, "composeApp/src/commonMain/kotlin")

        // Load the stored paths using PathStorage
        val settings = PathStorage.load(project)

        // Set the paths for each chooser
        listOf(
            childFileChooser to "childFile",
            configFileChooser to "configFile",
            navRootFileChooser to "navRootFile",
            rootComponentFileChooser to "rootComponentFile",
            childFunctionDirChooser to "childFunctionDir",
            composableFilesDirChooser to "composableFileDir"
        ).forEach { (chooser, key) ->
            val saved = settings?.let {
                when (key) {
                    "childFile" -> it.childFile
                    "configFile" -> it.configFile
                    "navRootFile" -> it.navRootFile
                    "rootComponentFile" -> it.rootComponentFile
                    "childFunctionDir" -> it.childFunctionDir
                    "composableFileDir" -> it.composableFileDir
                    else -> null
                }
            }
            when {
                saved != null -> chooser.setPath(saved)
                composeRoot.exists() -> chooser.setPath(composeRoot.path)
            }
        }

        // Persist on file select
        listOf(
            "childFile" to childFileChooser,
            "configFile" to configFileChooser,
            "navRootFile" to navRootFileChooser,
            "rootComponentFile" to rootComponentFileChooser,
            "childFunctionDir" to childFunctionDirChooser,
            "composableFileDir" to composableFilesDirChooser
        ).forEach { (key, chooser) ->
            chooser.onPathChange = { newPath ->
                val currentPaths = settings ?: StoredPaths(null, null, null, null, null, null)
                val updatedPaths = when (key) {
                    "childFile" -> currentPaths.copy(childFile = newPath)
                    "configFile" -> currentPaths.copy(configFile = newPath)
                    "navRootFile" -> currentPaths.copy(navRootFile = newPath)
                    "rootComponentFile" -> currentPaths.copy(rootComponentFile = newPath)
                    "childFunctionDir" -> currentPaths.copy(childFunctionDir = newPath)
                    "composableFileDir" -> currentPaths.copy(composableFileDir = newPath)
                    else -> currentPaths
                }
                PathStorage.save(project, updatedPaths)
            }
        }
    }

}
