package com.aaditx23.autodecompose

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import java.io.File
import javax.swing.*

class NavigationDialog(val project: Project) : DialogWrapper(true) {

    val composableNameField = JTextField()
    val childFileChooser = TextFieldWithBrowseButton()
    val configFileChooser = TextFieldWithBrowseButton()
    val navRootFileChooser = TextFieldWithBrowseButton()
    val rootComponentFileChooser = TextFieldWithBrowseButton()
    val childFunctionDirChooser = TextFieldWithBrowseButton()
    val composableFileDirChooser = TextFieldWithBrowseButton()

    init {
        title = "Add Decompose Navigation Entry"
        init()
        setupFileChoosers()
    }

    private fun setupFileChoosers() {
        val descriptorFile = FileChooserDescriptorFactory.createSingleFileDescriptor()
        val descriptorDir = FileChooserDescriptorFactory.createSingleFolderDescriptor()

        val defaultPath = getDefaultPath()

        val lastPaths = PathStorage.load(project)

        fun setupChooser(chooser: TextFieldWithBrowseButton, title: String, descriptor: com.intellij.openapi.fileChooser.FileChooserDescriptor, last: String?, fallback: String) {
            chooser.addBrowseFolderListener(title, null, project, descriptor)
            chooser.text = last ?: fallback
        }

        setupChooser(childFileChooser, "Select Child File", descriptorFile, lastPaths?.childFile, "$defaultPath")
        setupChooser(configFileChooser, "Select Config File", descriptorFile, lastPaths?.configFile, "$defaultPath")
        setupChooser(navRootFileChooser, "Select Navigation Root", descriptorFile, lastPaths?.navRootFile, "$defaultPath")
        setupChooser(rootComponentFileChooser, "Select Root Component File", descriptorFile, lastPaths?.rootComponentFile, "$defaultPath")
        setupChooser(childFunctionDirChooser, "Select Child Function Dir", descriptorDir, lastPaths?.childFunctionDir, "$defaultPath")
        setupChooser(composableFileDirChooser, "Select Composable File Dir", descriptorDir, lastPaths?.composableFileDir, "$defaultPath")
    }

    private fun getDefaultPath(): String {
        val root = project.basePath ?: return ""
        val fallback = "$root/composeApp/src/commonMain/kotlin"
        val file = File(fallback)
        return if (file.exists()) file.absolutePath else root
    }

    override fun createCenterPanel(): JComponent {
        return panel {
            row("Composable Name:") {
                cell(composableNameField).resizableColumn().align(AlignX.FILL)
            }
            row("Composable File Directory:") {
                cell(composableFileDirChooser).resizableColumn().align(AlignX.FILL)
            }
            row("Child File:") {
                cell(childFileChooser).resizableColumn().align(AlignX.FILL)
            }
            row("Configuration File:") {
                cell(configFileChooser).resizableColumn().align(AlignX.FILL)
            }
            row("Navigation Root File:") {
                cell(navRootFileChooser).resizableColumn().align(AlignX.FILL)
            }
            row("Root Component File:") {
                cell(rootComponentFileChooser).resizableColumn().align(AlignX.FILL)
            }
            row("Child Function Directory:") {
                cell(childFunctionDirChooser).resizableColumn().align(AlignX.FILL)
            }

        }
    }
}
