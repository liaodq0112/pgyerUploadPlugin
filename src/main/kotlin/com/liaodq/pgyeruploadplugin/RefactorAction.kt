package com.liaodq.pgyeruploadplugin

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.ui.InputValidator
import com.intellij.openapi.ui.Messages
import java.io.File

/**
 *
 * Author : liaodq
 * Date   : 2023/6/17 10:16
 * Package: com.liaodq.pgyeruploadplugin
 * Desc   : 在APK文件右键=>Refactor=>UploadApkToPGY上传
 */
class RefactorAction : AnAction() {
    override fun actionPerformed(anActionEvent: AnActionEvent) {
        val project = anActionEvent.getData(PlatformDataKeys.PROJECT)
        val currentEditorFile = anActionEvent.getData(PlatformDataKeys.PSI_FILE)
        if (project != null && currentEditorFile != null) {

            if (AppSettingsState.instance.apiKey.isEmpty()) {
                val apiKey = Messages.showInputDialog(project, "API KEY:", "请输入API KEY",
                    Messages.getQuestionIcon(), "332c78434208e3055aba906612f21d34", object : InputValidator {
                        override fun checkInput(s: String?) = !(s == null || s.trim { it <= ' ' }.isEmpty())

                        override fun canClose(s: String?) = true
                    })
                if (apiKey.isNullOrEmpty()) {
                    Messages.showErrorDialog("API KEY不能为空", "错误提示")
                    return
                }
                AppSettingsState.instance.apiKey = apiKey

            }
            val buildUpdateDescription = Messages.showInputDialog(
                project,
                "更新说明:",
                "请输入更新说明",
                Messages.getQuestionIcon(),
                "",
                object : InputValidator {
                    override fun checkInput(s: String?) = true
                    override fun canClose(s: String?) = true
                })
            buildUpdateDescription?.let {
                UploadApk.uploadApk(
                    project,
                    currentEditorFile.virtualFile.path,
                    AppSettingsState.instance.apiKey,
                    it
                )
            }
        } else {
            Messages.showErrorDialog("Plase chose the Apk file need to upload", "Error Tips")
        }
    }

    override fun update(e: AnActionEvent) {
        val currentEditorFile = e.getData(PlatformDataKeys.PSI_FILE)
        if (currentEditorFile != null) {
            val currentEditorFileName = currentEditorFile.name
            println("file name = $currentEditorFileName")
            if (currentEditorFileName.endsWith(".apk")) {
                e.presentation.isEnabled = true
            } else {
                e.presentation.isEnabled = false
            }
        } else {
            e.presentation.isEnabled = false
        }
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return super.getActionUpdateThread()
    }
}