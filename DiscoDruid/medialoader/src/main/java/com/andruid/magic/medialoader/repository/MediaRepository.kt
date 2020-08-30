package com.andruid.magic.medialoader.repository

import android.app.Application
import android.content.ContentResolver
import android.net.Uri

abstract class MediaRepository<out T : Any> {
    protected abstract val projection: Array<String>
    protected abstract val uri: Uri
    protected abstract val baseSelection: String?

    protected lateinit var contentResolver: ContentResolver

    open fun init(application: Application) {
        contentResolver = application.contentResolver
    }

    abstract suspend fun getAllPagedContent(limit: Int = Int.MAX_VALUE, offset: Int = 0): List<T>

    protected abstract suspend fun fetchUtil(
        selection: String?,
        selectionArgs: Array<String>?,
        limit: Int,
        offset: Int
    ): List<T>

    protected abstract fun getSortOrder(limit: Int, offset: Int): String
}