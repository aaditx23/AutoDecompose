package com.aaditx23.autodecompose


import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtilCore
import com.intellij.psi.PsiManager
import com.intellij.psi.JavaDirectoryService
import org.jetbrains.kotlin.psi.KtFile

object PackageProvider {

    fun fromPsiFile(psiFile: PsiFile?): String? {
        return when (psiFile) {
            is KtFile -> psiFile.packageFqName.asString()
            else -> psiFile?.containingDirectory?.let { fromPsiDirectory(it) }
        }
    }

    fun fromPsiDirectory(directory: PsiDirectory?): String? {
        return directory?.let {
            JavaDirectoryService.getInstance().getPackage(it)?.qualifiedName
        }
    }

    fun fromPath(project: com.intellij.openapi.project.Project, path: String): String? {
        val virtualFile = com.intellij.openapi.vfs.VfsUtil.findFile(java.io.File(path).toPath(), true)
        val psiFile = virtualFile?.let { PsiManager.getInstance(project).findFile(it) }
        return fromPsiFile(psiFile)
    }
}
