// Copyright 2000-2022 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.liaodq.pgyeruploadplugin

import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import java.io.File
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Supports creating and managing a [JPanel] for the Settings Dialog.
 */
class AppSettingsComponent {
    val panel: JPanel
    private val myApiKeyText = JBTextField()
    private val myApkPathText = JBTextField()
    private val myPathStatus = JBCheckBox("选择文件夹，自动找到最新的APK")

    init {
        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("API  KEY: "), myApiKeyText, 1, false)
            .addLabeledComponent(JBLabel("APK PATH: "), myApkPathText, 1, false)
            .addComponent(myPathStatus, 1)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    val preferredFocusedComponent: JComponent
        get() = myApiKeyText

    var apiKey: String
        get() = myApiKeyText.text
        set(newText) {
            myApiKeyText.text = newText
        }
    var apkPath: String
        get() = path()
        set(newStatus) {
            path = newStatus
            myApkPathText.text = path()
        }
    var pathStatus: Boolean
        get() = myPathStatus.isSelected
        set(newStatus) {
            myApkPathText.text = path()
            myPathStatus.isSelected = newStatus
        }
    private var path = ""
    private fun path(): String {
        if (myPathStatus.isSelected) {
            val file = File(path)
            if (file.exists() && !file.isDirectory) {
                return file.parent
            }
        }
        return path
    }
}
