package com.aaditx23.autodecompose

import java.awt.BorderLayout
import java.io.File
import javax.swing.*
import javax.swing.filechooser.FileSystemView

class FileChooserWithButton(defaultLabel: String) : JPanel(BorderLayout()) {

    private val textField = JTextField()
    private val browseButton = JButton("Browse")

    var onPathChange: ((String) -> Unit)? = null

    val text: String
        get() = textField.text

    init {
        layout = BorderLayout(10, 0)

        textField.text = defaultLabel
        add(textField, BorderLayout.CENTER)
        add(browseButton, BorderLayout.EAST)

        browseButton.addActionListener {
            val chooser = JFileChooser(FileSystemView.getFileSystemView()).apply {
                fileSelectionMode = JFileChooser.FILES_AND_DIRECTORIES
                selectedFile = File(textField.text)
            }

            val result = chooser.showOpenDialog(this)
            if (result == JFileChooser.APPROVE_OPTION) {
                val selectedPath = chooser.selectedFile.absolutePath
                setPath(selectedPath)
                onPathChange?.invoke(selectedPath)
            }
        }
    }

    fun setPath(path: String) {
        textField.text = path
    }
}
