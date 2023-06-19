package com.liaodq.pgyeruploadplugin

import com.google.gson.Gson
import com.intellij.ide.BrowserUtil
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.InputValidator
import com.intellij.openapi.ui.Messages
import okhttp3.*
import okhttp3.MultipartBody.Companion.FORM
import okio.Buffer
import okio.BufferedSink
import okio.Source
import okio.source
import java.io.File
import java.io.IOException
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

class UploadApk {

    interface ProgressListener {
        fun onProgress(totalBytes: Long, remainingBytes: Long, done: Boolean)
    }

    companion object {
        private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
            .readTimeout(100, TimeUnit.SECONDS) //设置读取超时时间
            .writeTimeout(100, TimeUnit.SECONDS)
            .connectTimeout(100, TimeUnit.SECONDS)
            .build()

        @JvmStatic
        fun uploadApk(project: Project, path: String, apiKey: String, buildUpdateDescription: String) {
            var call: Call? = null
            var file = File(path)
            if(AppSettingsState.instance.pathStatus){
                if(!file.isDirectory){
                    file = file.parentFile
                }
                val list = file.listFiles()?.filter {
                    it.name.endsWith(".apk")
                }?.sortedBy {
                    println(it.name + "  " + it.lastModified())
                    it.lastModified()
                } ?: arrayListOf<File>()
                if (list.isNotEmpty()) {
                    file = list.last()
                }
            }
//            if (AppSettingsState.instance.pathStatus && !file.isDirectory) {
//                file = file.parentFile
//            }
//            if (file.isDirectory) {
//                val list = file.listFiles()?.filter {
//                    it.name.endsWith(".apk")
//                }?.sortedBy {
//                    println(it.name + "  " + it.lastModified())
//                    it.lastModified()
//                } ?: arrayListOf<File>()
//                if (list.isNotEmpty()) {
//                    file = list.last()
//                }
//            }

            if (file.isDirectory || !file.exists()) {
                Messages.showErrorDialog("请选择正确的APK路径", "错误提示")
                return
            }
            println("uploading " + file.name)
            ProgressManager.getInstance().run(object : Task.Backgroundable(project, "正在上传...") {
                override fun run(progressIndicator: ProgressIndicator) {
                    // start your process
                    progressIndicator.text = "上传中!"
                    progressIndicator.isIndeterminate = false
                    val builder: MultipartBody.Builder = MultipartBody.Builder().setType(FORM)
//                    val file = File(currentEditorFile.virtualFile.path)
                    builder.addFormDataPart(
                        "file",
                        file.name,
                        createCustomRequestBody(FORM, file, object : ProgressListener {
                            override fun onProgress(totalBytes: Long, remainingBytes: Long, done: Boolean) {
                                val progress = (totalBytes - remainingBytes) / totalBytes.toDouble()
                                progressIndicator.fraction = progress
                                progressIndicator.text =
                                    if (done) "Upload completed" else readableFileSize(totalBytes - remainingBytes) + "/" + readableFileSize(
                                        totalBytes
                                    ) + "   " + ((totalBytes - remainingBytes) * 100 / totalBytes).toInt() + "%"
                                try {
                                    progressIndicator.checkCanceled()
                                } catch (e: ProcessCanceledException) {
                                    e.printStackTrace()
                                    call?.cancel()
                                }
                            }
                        })
                    )
                    builder.addFormDataPart("_api_key", apiKey)
                    builder.addFormDataPart("buildInstallType", "1")
//                    builder.addFormDataPart("_api_key", apiKey)
//                    builder.addFormDataPart("buildInstallType", if (pw.isNullOrEmpty()) "1" else "2")
//                    if (!pw.isNullOrEmpty()) {
//                        builder.addFormDataPart("buildPassword", pw)
//                    }
                    if (!buildUpdateDescription.isNullOrEmpty()) {
                        builder.addFormDataPart("buildUpdateDescription", buildUpdateDescription)
                    }
                    val requestBody: RequestBody = builder.build()
                    val request: Request = Request.Builder().url("https://www.pgyer.com/apiv2/app/upload") //地址
                        .post(requestBody).build()
                    val response: Response?
                    try {
                        call = okHttpClient.newCall(request)
                        response = call?.execute()
                        val pgyResponse = Gson().fromJson(
                            response?.body?.string(), PgyResponse::class.java
                        )
                        response?.close()
                        if (pgyResponse.getCode() == 0) {
                            progressIndicator.text = "上传成功!"
                            ApplicationManager.getApplication().invokeLater {
                                Messages.showMessageDialog(project, "上传成功!", "(*^_^*)", null)
                                BrowserUtil.browse(
                                    "https://www.pgyer.com/" + pgyResponse.getData()?.buildShortcutUrl, project
                                )
                            }
                        } else {
                            ApplicationManager.getApplication()
                                .invokeLater { Messages.showErrorDialog(pgyResponse.getMessage(), "上传失败！") }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        if (e.toString().contains("closed")) {
                            //如果是主动取消的情况下
                            ApplicationManager.getApplication()
                                .invokeLater { Messages.showMessageDialog(project, "Upload canceled", "(*^_^*)", null) }
                        } else {
                            ApplicationManager.getApplication()
                                .invokeLater { Messages.showErrorDialog(e.message, "上传失败") }
                        }
                    }
                }
            })
        }

        @JvmStatic
        fun createCustomRequestBody(contentType: MediaType?, file: File, listener: ProgressListener): RequestBody {
            return object : RequestBody() {
                override fun contentType(): MediaType? {
                    return contentType
                }

                override fun contentLength(): Long {
                    return file.length()
                }

                @Throws(IOException::class)
                override fun writeTo(sink: BufferedSink) {
                    val source: Source
                    try {
                        source = file.source()
                        //sink.writeAll(source);
                        val buf = Buffer()
                        var remaining = contentLength()
                        var readCount: Long
                        while (source.read(buf, 2048).also { readCount = it } != -1L) {
                            sink.write(buf, readCount)
                            listener.onProgress(
                                contentLength(),
                                readCount.let { remaining -= it; remaining },
                                remaining == 0L
                            )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        @JvmStatic
        fun readableFileSize(size: Long): String {
            if (size <= 0) return "0"
            val units = arrayOf("B", "kB", "MB", "GB", "TB")
            val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
            return DecimalFormat("#,##0.#").format(
                size / Math.pow(
                    1024.0,
                    digitGroups.toDouble()
                )
            ) + " " + units[digitGroups]
        }
    }
}
