package com.liaodq.pgyeruploadplugin

/**
 *
 * Author : liaodq
 * Date   : 2023/6/12 20:00
 * Package: com.liaodq.pgyeruploadplugin
 * Desc   : something
 */
class PgyResponse {
    private var code = 0
    private var data: Data? = null
    fun setCode(code: Int) {
        this.code = code
    }

    fun setData(data: Data?) {
        this.data = data
    }

    fun setMessage(message: String?) {
        this.message = message
    }

    private var message: String? = null
    fun getCode(): Int {
        return code
    }

    fun getData(): Data? {
        return data
    }

    fun getMessage(): String? {
        return message
    }
}

