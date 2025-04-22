package com.aaditx23.autodecompose


import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtilCore
import com.intellij.psi.PsiManager
import com.intellij.psi.JavaDirectoryService
import org.jetbrains.kotlin.psi.KtFile
import java.io.File

object PackageProvider {

    fun fromPsiFile(psiFile: PsiFile?): String? {
        return when (psiFile) {
            is KtFile -> psiFile.packageFqName.asString()
            else -> psiFile?.containingDirectory?.let { fromPsiDirectory(it) }
        }
    }

    fun fromPsiDirectory(directory: PsiDirectory?): String? {
        if (directory == null) return null

        return ReadAction.compute<String?, Throwable> {
            JavaDirectoryService.getInstance().getPackage(directory)?.qualifiedName
                ?: fallbackPackageFromPath(directory)
        }
    }

    private fun fallbackPackageFromPath(directory: PsiDirectory): String? {
        val path = directory.virtualFile.path
        val keyword = "src/commonMain/kotlin/"
        val idx = path.indexOf(keyword)
        if (idx == -1) return null
        val packagePath = path.substring(idx + keyword.length)
        return packagePath.replace('/', '.')
    }

    fun fromPath(project: Project, path: String): String? {
        val file = File(path)
        val virtualFile = VfsUtil.findFile(file.toPath(), true) ?: return null

        // If it's a file, get its directory
        val targetDir = if (file.isDirectory) virtualFile else virtualFile.parent
        val psiDir = PsiManager.getInstance(project).findDirectory(targetDir) ?: return null

        return fromPsiDirectory(psiDir)
    }
    fun fromPath(project: Project, path: String, callback: (String?) -> Unit) {
        ApplicationManager.getApplication().executeOnPooledThread {
            val file = File(path)
            val virtualFile = VfsUtil.findFile(file.toPath(), true)

            if (virtualFile == null) {
                callback(null)
                return@executeOnPooledThread
            }

            val targetDir = if (file.isDirectory) virtualFile else virtualFile.parent

            ApplicationManager.getApplication().runReadAction {
                val psiDir = PsiManager.getInstance(project).findDirectory(targetDir)
                val packageName = psiDir?.let { fromPsiDirectory(it) }

                ApplicationManager.getApplication().invokeLater {
                    callback(packageName)
                }
            }
        }
    }



}
