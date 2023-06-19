package com.liaodq.pgyeruploadplugin

/**
 *
 * Author : liaodq
 * Date   : 2023/6/12 20:00
 * Package: com.liaodq.pgyeruploadplugin
 * Desc   : something
 */
data class Data(
    var buildName: String? = null,
    var buildVersion: String? = null,
    @JvmField
    var buildShortcutUrl: String? = null,
    var buildUpdateDescription: String? = null,
    var buildQRCodeURL: String? = null,
    var buildKey: String? = null,
    var buildFileName: String? = null,
    var buildFileKey: String? = null,
    var buildFileSize: String? = null,
    var buildIdentifier: String? = null,
    var buildVersionNo: String? = null,
    var buildDescription: String? = null,
    var buildScreenshots: String? = null,
    var buildIsFirst: String? = null,
    var buildType: String? = null,
    var buildUpdated: String? = null,
    var buildBuildVersion: String? = null,
    var buildCreated: String? = null,
    var buildIsLastest: String? = null,
    var buildIcon: String? = null
)
