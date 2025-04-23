package com.aaditx23.autodecompose

import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VfsUtil
import java.awt.BorderLayout
import java.io.File
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JTextField

class FileChooserWithButton(
    private val project: Project,
    defaultLabel: String = "",
) : JPanel(BorderLayout(10, 0)) {

    private val textField = JTextField()
    private val browseButton = JButton("Browse")

    var onPathChange: ((String) -> Unit)? = null

    val text: String
        get() = textField.text

    init {
        textField.text = defaultLabel
        add(textField, BorderLayout.CENTER)
        add(browseButton, BorderLayout.EAST)

        browseButton.addActionListener {
            val descriptor: FileChooserDescriptor =
                FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor()
                    .withTitle(defaultLabel)
                    .withShowHiddenFiles(false)

            val toSelect = VfsUtil.findFile(File(textField.text).toPath(), true)
            FileChooser.chooseFile(descriptor, project, toSelect) { selectedFile: VirtualFile? ->
                selectedFile?.let {
                    val selectedPath = it.path
                    setPath(selectedPath)  // This now both updates the text field and triggers the callback
                }
            }
        }
    }

    fun setPath(path: String) {
        textField.text = path
        println("Setting path: $path")  // Debugging line
        onPathChange?.invoke(path)
    }

}
