package com.tatasky.binge.utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import com.irdeto.itac.ITACAgentListener
import com.irdeto.itac.ITACResult
import com.irdeto.itac.ITACStatus
import com.tatasky.binge.ui.base.MyApp
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

fun getFolderPath(context: Context): String {
    val storeFilePath = context.getFilesDir().getAbsolutePath()
    val am = context.getAssets()

    var assetnames: List<String>? = null
    try {
        assetnames = ArrayList(Arrays.asList(*am.list("data")!!))
        val dir = context.getFilesDir()
        for (assetname in assetnames) {
            if (assetname.contains("acv")) {
                val file = File(dir, assetname)
                if (file.exists()) {
                    file.delete()
                }

                d("ACA", "copy assets : $assetname")
                val inputStream = am.open("data/$assetname")
                val buf = ByteArray(1024)
                val fos = FileOutputStream(file)
                var read = 0
                do {
                    read = inputStream.read(buf, 0, 1024)
                    if (read > 0) {
                        fos.write(buf, 0, read)
                    }
                } while (read > 0)
                inputStream.close()
                fos.flush()
                fos.close()

            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }

    return storeFilePath
}


fun acaSecurityCheck(): String {
    var message = ""
    val itacStatus = ITACStatus()
    val itacResult = MyApp.getITACAgent().check({ message = ""}, null , itacStatus)
    if(itacResult == ITACResult.ITAC_OK) {
        message = if (itacStatus.isRooted) {
            return "We have detected a rooted device. Please switch to a non-rooted device to use this application."
        } else ""

        message = if (itacStatus.isHookDetected) {
            return "We have detected security issues on this device. Switch to a secure device to use this application. Please contact customer care for more."
        } else ""

        message = if (itacStatus.isIVFailed) {
            return "We have detected security issues on this device. Switch to a secure device to use this application. Please contact customer care for more."
        } else ""

        message = if (itacStatus.isDebugDetected) {
            return "We have detected security issues on this device. Switch to a secure device to use this application. Please contact customer care for more."
        } else ""
    } else {
        message = "Sorry you can\\'t use this app on this device."
    }
    return message
}

private fun showIrdetoDialog(message: String, context: Context) {
    val irdetoDialog = AlertDialog.Builder(context).create()
    irdetoDialog.setButton(
        DialogInterface.BUTTON_NEUTRAL, "OK"
    ) { dialog, which -> }
    irdetoDialog.setMessage(message)
    irdetoDialog.show()
}
