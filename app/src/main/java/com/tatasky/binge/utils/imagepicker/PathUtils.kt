package com.tatasky.binge.utils.imagepicker

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import java.io.File
import java.net.URISyntaxException

/***
 * to get real path or physical path of image
 * @param uri uri
 * @return path of bitmap
 */
fun getRealPathFromURI(context: Context, uri: Uri?): File? {
    return if (uri != null) {
        try {
            val filePath = getFilePath(context, uri)
            if (filePath != null) File(filePath) else null
        } catch (e: URISyntaxException) {
            e.printStackTrace()
            null
        }

    } else
        null
}

@Throws(URISyntaxException::class)
private fun getFilePath(context: Context, uri: Uri): String? {
    var uri = uri
    var selection: String? = null
    var selectionArgs: Array<String>? = null

    try {

        // Uri is different in versions after KITKAT (Android 4.4), we need to
        if (Build.VERSION.SDK_INT >= 19 && DocumentsContract.isDocumentUri(
                context.applicationContext,
                uri
            )
        ) {
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                return Environment.getExternalStorageDirectory().toString() + "/" + split[1]

            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                uri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    java.lang.Long.valueOf(id)
                )

            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]
                if ("image" == type) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                selection = "_id=?"
                selectionArgs = arrayOf(split[1])
            }
        }
        if ("content".equals(uri.scheme!!, ignoreCase = true)) {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            var cursor: Cursor? = null
            try {
                cursor = context.contentResolver
                    .query(uri, projection, selection, selectionArgs, null)
                if (cursor != null) {
                    val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    if (cursor.moveToFirst()) {
                        return cursor.getString(column_index)
                    }
                } else
                    return null

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cursor?.close()
            }

        } else if ("file".equals(uri.scheme!!, ignoreCase = true)) {
            return uri.path
        }
        return null
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }

}

private fun isExternalStorageDocument(uri: Uri): Boolean {
    return "com.android.externalstorage.documents" == uri.authority
}

private fun isDownloadsDocument(uri: Uri): Boolean {
    return "com.android.providers.downloads.documents" == uri.authority
}

private fun isMediaDocument(uri: Uri): Boolean {
    return "com.android.providers.media.documents" == uri.authority
}
