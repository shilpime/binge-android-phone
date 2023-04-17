package com.tatasky.binge.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Base64
import androidx.core.content.FileProvider
import okhttp3.ResponseBody
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


@Throws(IOException::class)
fun downloadFile(body: ResponseBody, context: Context, id: String): Boolean {
    var bis: BufferedInputStream? = null
    var output: FileOutputStream? = null
    try {
        var count: Int
        val data = ByteArray(1024 * 4)
        val fileSize = body.contentLength()
        bis = BufferedInputStream(body.byteStream(), 1024 * 8)

        val myDir =
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        /* val outputFile =
             Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
         )*/
        if (!myDir!!.exists()) {
            if (myDir.mkdirs()) {
                // directory exists, can place any files here
            }
        }
       val file = File(myDir, id.plus(".pdf"))
        if (file.exists()) file.delete()
        file.createNewFile()

        output = FileOutputStream(file)
        var total: Long = 0
        val startTime = System.currentTimeMillis()
        var timeCount = 1

        while (true) {
            count = bis.read(data)
            if (count == -1)
                break
            total += count.toLong()
            val totalFileSize = (fileSize / Math.pow(1024.0, 2.0)).toInt()
            val current = Math.round(total / Math.pow(1024.0, 2.0)).toDouble()

            val progress = (total * 100 / fileSize).toInt()

            val currentTime = System.currentTimeMillis() - startTime

            //  val download = Download()
            //   download.setTotalFileSize(totalFileSize)

            if (currentTime > 1000 * timeCount) {

                // download.setCurrentFileSize(current.toInt())
                //download.setProgress(progress)

                timeCount++
            }

            output.write(data, 0, count)
        }
        return true

    } catch (e: Exception) {
        e.printStackTrace()
        return false
    } finally {
        if (output != null) {
            output.flush()
            output.close()
        }
        if (bis != null)
            bis.close()
    }
}

fun downloadFileBase64(base64Invoice:String, context: Context,fileName:String): Uri? {
    val myDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val dwldsPath: File = File(myDir,fileName)
    var os: FileOutputStream?=null
    var pdfAsBytes: ByteArray?=null
    var isDownload:Boolean=false
    try {
        pdfAsBytes = Base64.decode(base64Invoice, 0)
        os = FileOutputStream(dwldsPath, false)
        os.write(pdfAsBytes)
        isDownload=true
    }catch (e:Exception){
        isDownload=false
    }finally {
        os?.flush()
        os?.close()
        if (isDownload) {
            val photoURI = FileProvider.getUriForFile(
                    context,
                    context.packageName + ".fileprovider",dwldsPath
            )
            return photoURI
        }
        else null
    }
    return null
}


