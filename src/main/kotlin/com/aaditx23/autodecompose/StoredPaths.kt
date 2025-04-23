package com.aaditx23.autodecompose

import com.intellij.openapi.project.Project
import java.io.File
import java.util.*

data class StoredPaths(
    var childFile: String = "",
    var configFile: String = "",
    var navRootFile: String = "",
    var rootComponentFile: String = "",
    var childFunctionDir: String = "",
    var composableFileDir: String = ""
){
    fun default(defaultPath: String): StoredPaths {
        return StoredPaths(
            childFile = defaultPath,
            configFile = defaultPath,
            navRootFile = defaultPath,
            rootComponentFile = defaultPath,
            childFunctionDir = defaultPath,
            composableFileDir = defaultPath,
        )
    }

    fun isDefaultPath(path: String): Boolean {
        return (
                childFile == path
                && configFile == path
                && navRootFile == path
                && rootComponentFile == path
                && childFunctionDir == path
                && composableFileDir == path
                )
    }

    fun print(){
        println("ChildFile: $childFile")
        println("ConfigFile: $configFile")
        println("NavRootFile: $navRootFile")
        println("RootComponentFile: $rootComponentFile")
        println("ComposableFileDir: $composableFileDir")
        println("ChildFunctionDir: $childFunctionDir")
    }


}

object PathStorage {
    private const val FILE_NAME = ".decompose_plugin_paths.properties"

    fun load(project: Project, defaultPath: String): StoredPaths {
        val file = File(project.basePath, FILE_NAME)
        if (!file.exists()) return StoredPaths().default(defaultPath)

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
        println("File updated with \n ")
        paths.print()
    }
}
