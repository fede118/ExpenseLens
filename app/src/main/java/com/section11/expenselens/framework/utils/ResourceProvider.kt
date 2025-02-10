package com.section11.expenselens.framework.utils

import android.content.Context

interface ResourceProvider {
    fun getString(resId: Int, vararg args: Any): String
}

class ResourceProviderImpl(private val context: Context) : ResourceProvider {
    override fun getString(resId: Int, vararg args: Any): String {
        return context.getString(resId, *args)
    }
}
