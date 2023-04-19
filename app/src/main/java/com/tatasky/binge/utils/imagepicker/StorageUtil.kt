package com.tatasky.binge.utils.imagepicker

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

private val DIR_ANDROID = "Android"
private val DIR_DATA = "data"
private val DIR_FILES = "files"

@Synchronized
fun getExternalStorageAppFilesFile(
    context: Context?,
    fileName: String?
): File? {

    if (context == null) return null
    if (fileName == null) return null
    if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
        val dirs = buildExternalStorageAppFilesDirs(
            Environment.getExternalStorageDirectory()
                .absolutePath,
            context.packageName
        )
        return File(dirs, fileName)
    }
    return null
}


@Synchronized
private fun buildExternalStorageAppFilesDirs(
    externalStoragePath: String, packageName: String
): File {
    return buildPath(
        externalStoragePath,
        DIR_ANDROID, DIR_DATA,
        packageName, DIR_FILES
    )
}

@Throws(IOException::class)
fun createImageFile(context: Context?): File? {
    // Create an image file name
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val storageDir: File? = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(
        "JPEG_${timeStamp}_", /* prefix */
        ".jpg", /* suffix */
        storageDir /* directory */
    )
}


@Synchronized
private fun buildPath(base: String, vararg segments: String): File {
    var cur = File(base)
    for (segment in segments) {
        cur = File(cur, segment)
    }
    return cur
}