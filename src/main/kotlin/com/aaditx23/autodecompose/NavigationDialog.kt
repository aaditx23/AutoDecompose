package com.aaditx23.autodecompose



import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.panel
import javax.swing.*
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.dsl.builder.AlignX

class NavigationDialog : DialogWrapper(true) {

    val composableNameField = JTextField()
    val childFileChooser = TextFieldWithBrowseButton()
    val configFileChooser = TextFieldWithBrowseButton()
    val navRootFileChooser = TextFieldWithBrowseButton()
    val rootComponentFileChooser = TextFieldWithBrowseButton()

    init {
        title = "Add Decompose Navigation Entry"
        init()
        setupFileChoosers()
    }

    private fun setupFileChoosers() {
        val descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor()
        childFileChooser.addBrowseFolderListener("Select Child File", null, null, descriptor)
        configFileChooser.addBrowseFolderListener("Select Config File", null, null, descriptor)
        navRootFileChooser.addBrowseFolderListener("Select Navigation Root", null, null, descriptor)
        rootComponentFileChooser.addBrowseFolderListener("Select Root Component", null, null, descriptor)
    }

    override fun createCenterPanel(): JComponent {
        return panel {
            row("Composable Name:") {
                cell(composableNameField)
                    .resizableColumn()
                    .align(AlignX.FILL)
            }
            row("Child File:") {
                cell(childFileChooser)
                    .resizableColumn()
                    .align(AlignX.FILL)
            }
            row("Configuration File:") {
                cell(configFileChooser)
                    .resizableColumn()
                    .align(AlignX.FILL)
            }
            row("Navigation Root File:") {
                cell(navRootFileChooser)
                    .resizableColumn()
                    .align(AlignX.FILL)
            }
            row("Root Component File:") {
                cell(rootComponentFileChooser)
                    .resizableColumn()
                    .align(AlignX.FILL)
            }
        }
    }
}
