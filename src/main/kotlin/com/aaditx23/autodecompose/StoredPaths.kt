package com.aaditx23.autodecompose

import com.intellij.openapi.project.Project
import java.io.File
import java.util.*

data class StoredPaths(
    val childFile: String?,
    val configFile: String?,
    val navRootFile: String?,
    val rootComponentFile: String?,
    val childFunctionDir: String?,
    val composableFileDir: String?
)

object PathStorage {
    private const val FILE_NAME = ".decompose_plugin_paths.properties"

    fun load(project: Project): StoredPaths? {
        val file = File(project.basePath, FILE_NAME)
        if (!file.exists()) return null

        val props = Properties().apply {
            file.inputStream().use { load(it) }
        }

        return StoredPaths(
            props.getProperty("childFile"),
            props.getProperty("configFile"),
            props.getProperty("navRootFile"),
            props.getProperty("rootComponentFile"),
            props.getProperty("childFunctionDir"),
            props.getProperty("composableFileDir")
        )
    }

    fun save(project: Project, paths: StoredPaths) {
        val file = File(project.basePath, FILE_NAME)
        val props = Properties().apply {
            put("childFile", paths.childFile ?: "")
            put("configFile", paths.configFile ?: "")
            put("navRootFile", paths.navRootFile ?: "")
            put("rootComponentFile", paths.rootComponentFile ?: "")
            put("childFunctionDir", paths.childFunctionDir ?: "")
            put("composableFileDir", paths.composableFileDir ?: "")
        }
        file.outputStream().use { props.store(it, "Decompose Plugin Paths") }
    }
}
