package com.tatasky.binge.utils.imagepicker

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import java.io.File

fun resize(image: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
    @Suppress("NAME_SHADOWING") var image = image
    if (maxHeight > 0 && maxWidth > 0) {
        val width = image.width
        val height = image.height
        val ratioBitmap = width.toFloat() / height.toFloat()
        val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()

        var finalWidth = maxWidth
        var finalHeight = maxHeight
        if (ratioMax > 1) {
            finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
        } else {
            finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
        }
        image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true)
        return image
    } else {
        return image
    }
}

fun loadBitmapFromPath(absoluteFilePath: String?): Bitmap?{
    if(absoluteFilePath== null)
        return null
    val sd = Environment.getExternalStorageDirectory()
    val image = File(absoluteFilePath)
    val bmOptions = BitmapFactory.Options()
    var bitmap = BitmapFactory.decodeFile(image.absolutePath, bmOptions)
    if(bitmap != null)
        bitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, true)
    return bitmap
}