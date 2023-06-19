// Copyright 2000-2022 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.liaodq.pgyeruploadplugin

import com.intellij.openapi.options.Configurable
import org.jetbrains.annotations.Nls
import javax.swing.JComponent

/**
 * Provides controller functionality for application settings.
 */
class AppSettingsConfigurable : Configurable {
    private var mySettingsComponent: AppSettingsComponent? = null

    // A default constructor with no arguments is required because this implementation
    // is registered as an applicationConfigurable EP
    override fun getDisplayName(): @Nls(capitalization = Nls.Capitalization.Title) String? {
        return "UploadApkToPGY Settings"
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return mySettingsComponent?.preferredFocusedComponent
    }

    override fun createComponent(): JComponent? {
        mySettingsComponent = AppSettingsComponent()
        return mySettingsComponent?.panel
    }

    override fun isModified(): Boolean {
        val settings = AppSettingsState.instance
        return mySettingsComponent?.apiKey != settings.apiKey || (mySettingsComponent?.apkPath != settings.apkPath) || mySettingsComponent?.pathStatus != settings.pathStatus
    }

    override fun apply() {
        val settings = AppSettingsState.instance
        settings.apiKey = mySettingsComponent?.apiKey ?: ""
        settings.pathStatus = mySettingsComponent?.pathStatus ?: false
        settings.apkPath = mySettingsComponent?.apkPath ?: ""
    }

    override fun reset() {
        val settings = AppSettingsState.instance
        mySettingsComponent?.apiKey = settings.apiKey
        mySettingsComponent?.apkPath = settings.apkPath
        mySettingsComponent?.pathStatus = settings.pathStatus
    }

    override fun disposeUIResources() {
        mySettingsComponent = null
    }
}
