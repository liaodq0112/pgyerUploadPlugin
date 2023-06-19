package com.liaodq.pgyeruploadplugin

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.InputValidator
import com.intellij.openapi.ui.Messages

/**
 *
 * Author : liaodq
 * Date   : 2023/6/17 10:07
 * Package: com.liaodq.pgyeruploadplugin
 * Desc   : 右键UploadApkToPGY上传
 */
class PopupMenuAction : AnAction() {


    override fun actionPerformed(anActionEvent: AnActionEvent) {
        val project = anActionEvent.getData(PlatformDataKeys.PROJECT)
        if (project != null) {
            if (AppSettingsState.instance.apiKey.isEmpty()) {
                val apiKey = Messages.showInputDialog(project, "API KEY:", "请输入API KEY",
                    Messages.getQuestionIcon(), "", object : InputValidator {
                        override fun checkInput(s: String?) = !(s == null || s.trim { it <= ' ' }.isEmpty())

                        override fun canClose(s: String?) = true
                    })
                if (apiKey.isNullOrEmpty()) {
                    Messages.showErrorDialog("API KEY不能为空", "错误提示")
                    return
                }
                AppSettingsState.instance.apiKey = apiKey

            }
            if (AppSettingsState.instance.apkPath.isEmpty() || (!AppSettingsState.instance.pathStatus && !AppSettingsState.instance.apkPath.endsWith(
                    ".apk"
                ))
            ) {
                val chooserDescriptor = FileChooserDescriptor(true, false, false, false, false, false)
                chooserDescriptor.withFileFilter {
                    it.name.endsWith(".apk")
                }
                val file = FileChooser.chooseFile(
                    chooserDescriptor,
                    project,
                    project.guessProjectDir()?.findChild("app")?.findChild("build")?.findChild("outputs")
                        ?.findChild("apk")
                )
                if (file != null && file.name.endsWith(".apk")) {
                    AppSettingsState.instance.apkPath = file.path
                } else {
                    Messages.showErrorDialog("请选择正确的APK安装包", "错误提示")
                    return
                }
            }

//            if (AppSettingsState.instance.apkPath.isEmpty()) {
//                val path = Messages.showInputDialog(project, "APK路径:", "请输入APK路径",
//                    Messages.getQuestionIcon(),
//                    project.guessProjectDir()?.path + "/app/build/outputs/apk/", object : InputValidator {
//                        override fun checkInput(s: String?) = !(s == null || s.trim { it <= ' ' }.isEmpty())
//
//                        override fun canClose(s: String?) = true
//                    })
//                val file = File(path)
//                if (path.isNullOrEmpty() || !file.exists()) {
//                    Messages.showErrorDialog("请选择正确的APK路径", "错误提示")
//                    return
//                }
//                AppSettingsState.instance.apkPath = path
//            }

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
                    AppSettingsState.instance.apkPath,
                    AppSettingsState.instance.apiKey,
                    it
                )
            }
        }

    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        val editor = e.getData(CommonDataKeys.EDITOR)
        e.presentation.isEnabledAndVisible = project != null && editor != null
    }

}
