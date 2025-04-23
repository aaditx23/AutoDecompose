package com.aaditx23.autodecompose

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import java.awt.Dimension
import java.io.File
import javax.swing.*

class NavigationDialog(val project: Project) : DialogWrapper(true) {

    companion object {
        private var instance: NavigationDialog? = null


        fun getInstance(project: Project): NavigationDialog {
            return instance ?: NavigationDialog(project).also {
                instance = it
            }
        }
    }

    private var paths: StoredPaths = StoredPaths().default(getDefaultPath())


    val composableNameField = JTextField(20)
    val childFileChooser = TextFieldWithBrowseButton()
    val configFileChooser = TextFieldWithBrowseButton()
    val navRootFileChooser = TextFieldWithBrowseButton()
    val rootComponentFileChooser = TextFieldWithBrowseButton()
    val childFunctionDirChooser = TextFieldWithBrowseButton()
    val composableFilesDirChooser = TextFieldWithBrowseButton()

    val statusLabel = JLabel("Ready")
    val triggerActionButton = JButton("Run")

    init {
        println("INIT INIT INIT")
        title = "Add Decompose Navigation"
        instance = this
        println(getDefaultPath())
        paths = PathStorage.load(project, getDefaultPath())
        setupFileChoosers()

        init()
        println("INIT COMPLETE")

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

    private fun setupFileChoosers() {
        val descriptorFile = FileChooserDescriptorFactory.createSingleFileDescriptor()
        val descriptorDir = FileChooserDescriptorFactory.createSingleFolderDescriptor()

        val defaultPath = getDefaultPath()

        fun setupChooser(
            chooser: TextFieldWithBrowseButton,
            title: String,
            descriptor: com.intellij.openapi.fileChooser.FileChooserDescriptor,
            initial: String?,
            onUpdate: (String) -> Unit
        ) {
            chooser.addBrowseFolderListener(title, null, project, descriptor)
            chooser.text = initial ?: defaultPath

            chooser.textField.document.addDocumentListener(object : javax.swing.event.DocumentListener {
                override fun insertUpdate(e: javax.swing.event.DocumentEvent?) = update()
                override fun removeUpdate(e: javax.swing.event.DocumentEvent?) = update()
                override fun changedUpdate(e: javax.swing.event.DocumentEvent?) = update()

                private fun update() {
                    onUpdate(chooser.text)
                    savePaths()
                }
            })
        }

        setupChooser(childFileChooser, "Select Child File", descriptorFile, paths.childFile) {
            paths.childFile = it
            println("ChildFile ${paths.childFile}")
        }

        setupChooser(configFileChooser, "Select Config File", descriptorFile, paths.configFile) {
            paths.configFile = it
            println("ConfigFile ${paths.configFile}")
        }

        setupChooser(navRootFileChooser, "Select Navigation Root", descriptorFile, paths.navRootFile) {
            paths.navRootFile = it
            println("NavRootFile ${paths.navRootFile}")
        }

        setupChooser(rootComponentFileChooser, "Select Root Component File", descriptorFile, paths.rootComponentFile) {
            paths.rootComponentFile = it
            println("RootComponentFile ${paths.rootComponentFile}")
        }

        setupChooser(childFunctionDirChooser, "Select Child Function Dir", descriptorDir, paths.childFunctionDir) {
            paths.childFunctionDir = it
            println("ChildFunctionDir ${paths.childFunctionDir}")
        }

        setupChooser(composableFilesDirChooser, "Select Composable File Dir", descriptorDir, paths.composableFileDir) {
            paths.composableFileDir = it
            println("ComposableFileDir ${paths.composableFileDir}")
        }

        println("COMPLETE")
    }



    private fun getDefaultPath(): String {
        val base = File(project.basePath ?: return "")
        val composeRoot = File(base, "composeApp/src/commonMain/kotlin")
        return composeRoot.path
    }

    private fun savePaths() {
       PathStorage.save(project, paths)
    }
}
