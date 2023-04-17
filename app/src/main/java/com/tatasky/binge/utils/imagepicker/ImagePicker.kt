package com.tatasky.binge.utils.imagepicker

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.tatasky.binge.utils.d
import com.tatasky.binge.utils.e
import com.tatasky.binge.utils.e
import com.tatasky.binge.utils.showToast
import java.io.*
import kotlin.random.Random

object ImagePicker {

    private val TEMP_IMAGE_NAME = "temp.png"
    private val TAG = "ImagePickerLog"
    const val CAMERA_INTENT = 1
    const val GALLERY_INTENT = 2
    const val FILE_INTENT = 3
    const val PERMISSION_REQUEST_CODE = 100
    private val SIZE = 512

    private lateinit var mCurrentCameraPhotoPath: String

    val pickImageGalleryIntent: Intent
        get() {
            val getIntent = Intent(Intent.ACTION_GET_CONTENT)
            getIntent.type = "image/*"

            val pickIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickIntent.type = "image/*"

            val chooserIntent = Intent.createChooser(pickIntent, "Select Image")
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(getIntent))

            /*startActivityForResult(chooserIntent, PICK_IMAGE)
            val intent = Intent(
                Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            intent.type = "image/*"*/

             */
            return chooserIntent
        }

    fun getGalleryImage(activity: Activity, fragment: Fragment): Intent? {

//        if (checkRuntimePermission(activity, fragment)) {
        return pickImageGalleryIntent
//        }
//        return null
    }

    fun getPickImageCameraIntent(mContext: Context): Intent {
        //code to create camera intent
        val cameraInt = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if (cameraInt.resolveActivity(mContext.packageManager) !=
            null
        ) {

            val photoURI = FileProvider.getUriForFile(
                mContext,
                mContext.packageName + ".fileprovider",
                getTempFile(mContext)!!.apply {
                    mCurrentCameraPhotoPath = absolutePath
                }
            )

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                cameraInt.clipData = ClipData.newRawUri("", photoURI)
                cameraInt.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            if (photoURI != null) {
                cameraInt.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            }
        }

        return cameraInt
    }

    fun getPickFilesIntent(activity: Activity): Intent? {
        d(TAG, "getPickFilesIntent: Inside HPiker")
        val mimeType = "*/*"
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = mimeType
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        intent.addCategory(Intent.CATEGORY_OPENABLE)

        // special intent for Samsung file manager
        val sIntent = Intent("com.sec.android.app.myfiles.PICK_DATA")
        sIntent.putExtra("CONTENT_TYPE", mimeType)
        sIntent.addCategory(Intent.CATEGORY_DEFAULT)
        sIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)

        return null
    }

    //create temp file in android directory
    private fun getTempFile(context: Context): File? {
        return createImageFile(context)
    }

    fun onActivityResult(
        mContext: Context,
        requestCode: Int,
        resultCode: Int,
        data: Intent,
        mCallback: OnImagePicked
    ) {

        if (resultCode == Activity.RESULT_OK) {
            var bm: Bitmap?
            var fileInstance: File?
            var selectedFileUri: Uri?
            when (requestCode) {

                CAMERA_INTENT -> {
                    if (::mCurrentCameraPhotoPath.isInitialized) {
                        fileInstance = File(mCurrentCameraPhotoPath)
                        selectedFileUri = Uri.fromFile(fileInstance)
                        val rotation = getRotation(mContext, fileInstance.absolutePath, null, true)
                        bm = decodeBitmap(mCurrentCameraPhotoPath)
                        if (bm != null) {
                            bm = rotate(bm, rotation)
                            //save fixed orientation bitmap into file
                            saveBitmap(bm, fileInstance)
                            mCallback.onSuccess(fileInstance, bm)
                        } else
                            mCallback.onError("Cannot form bitmap")
                    } else {
                        mCallback.onError("Out of memory error, System Killed App")
                        showToast(
                            mContext,
                            "Try clicking image with lower resolution in camera app."
                        )
                    }
                }


                GALLERY_INTENT -> {

                    Handler(Looper.getMainLooper()).postDelayed({
                        var fileInstance1: File?
                        var bm1: Bitmap?

                        if (data != null) {
                            selectedFileUri = data.data
                            var fileName = "Temp" +System.currentTimeMillis()+ ".jpg"
                            data.data?.let { returnUri ->
                                mContext.contentResolver.query(returnUri, null, null, null, null)
                            }?.use { cursor ->
                                /*
                                 * Get the column indexes of the data in the Cursor,
                                 * move to the first row in the Cursor, get the data,
                                 * and display it.
                                 */
                                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                                cursor.moveToFirst()
                                fileName = cursor.getString(nameIndex)
                            }
                            fileInstance1 = File(mContext.cacheDir, fileName)
                            if (selectedFileUri != null) {
                                val rotation = getRotation(mContext, "", selectedFileUri, false)

                                bm1 = decodeSampledBitmapFromResource(
                                    mContext,
                                    selectedFileUri!!, SIZE, SIZE
                                )
                                if (bm1 != null) {
                                    bm1 = rotate(
                                        bm1, rotation
                                    )
                                    saveBitmap(bm1, fileInstance1)

                                    mCallback.onSuccess(fileInstance1, bm1)
                                } else
                                    mCallback.onError()
                            } else
                                mCallback.onError()
                        } else
                            mCallback.onError()

                    }, 500)


                }

                FILE_INTENT -> {
                    d(TAG, "onActivityResult: Inside HPiker")
                    selectedFileUri = data?.data
                    fileInstance = getRealPathFromURI(mContext, selectedFileUri)

                    if (fileInstance != null)
                        mCallback.onFileSuccess(fileInstance)
                    else
                        mCallback.onError()
                }
            }
        }
    }

    private fun getRealPathFromURI(context: Context, contentUri: Uri): String? {
        var cursor: Cursor? = null
        return try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(contentUri, proj, null, null, null)
            val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(column_index)
        } catch (exec: java.lang.Exception) {
            e(TAG, "getRealPathFromURI Exception : $exec")
            ""
        } finally {
            cursor?.close()
        }
    }

    private fun decodeBitmap(photoPath: String): Bitmap? {
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        options.inSampleSize = 8
        return BitmapFactory.decodeFile(photoPath, options)
    }

    private fun decodeFile(photoPath: String): Bitmap? {

        return BitmapFactory.decodeFile(photoPath)
    }

    private fun saveBitmap(bm: Bitmap?, file: File?) {
        val outStream: OutputStream
        try {
            outStream = FileOutputStream(file!!)
            if(file.name.contains(".png", true))
                bm!!.compress(Bitmap.CompressFormat.PNG, 75, outStream)
            else{
                bm!!.compress(Bitmap.CompressFormat.JPEG, 75, outStream)
            }
            outStream.flush()
            outStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    //to get rotation of bitmap
    private fun getRotation(
        context: Context,
        imagePath: String?,
        imageUri: Uri?,
        isCamera: Boolean
    ): Int {
        val rotation: Int
        if (isCamera) {
            rotation = getRotationFromCamera(context, imagePath)
        } else {
            rotation = getRotationFromGallery(context, imageUri)
        }
        d(TAG, "Image rotation: $rotation")
        return rotation
    }

    //to get rotation of bitmap capture mFrom camera
    private fun getRotationFromCamera(context: Context, imageFile: String?): Int {
        var rotate = 0
        try {
            //   context.contentResolver.notifyChange(imageFile!!, null)
            val exif = ExifInterface(imageFile!!)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )

            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_270 -> rotate = 270
                ExifInterface.ORIENTATION_ROTATE_180 -> rotate = 180
                ExifInterface.ORIENTATION_ROTATE_90 -> rotate = 90
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return rotate
    }

    //to get rotation of bitmap picked mFrom gallery
    private fun getRotationFromGallery(context: Context, imageUri: Uri?): Int {
        var result = 0
        val columns = arrayOf(MediaStore.Images.Media.ORIENTATION)
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(
                imageUri!!,
                columns, null, null, null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val orientationColumnIndex = cursor.getColumnIndex(columns[0])
                result = cursor.getInt(orientationColumnIndex)
            }
        } catch (e: Exception) {
            //Do nothing
        } finally {
            cursor?.close()
        }//End of try-catch block
        return result
    }

    //to rotate bitmap
    private fun rotate(bm: Bitmap, rotation: Int): Bitmap {
        if (rotation != 0) {
            val matrix = Matrix()
            matrix.postRotate(rotation.toFloat())
            return Bitmap.createBitmap(
                bm, 0, 0,
                bm.width,
                bm.height,
                matrix,
                true
            )
        }
        return bm
    }


    abstract class OnImagePicked {

        abstract fun onSuccess(imageFile: File?, bm: Bitmap?)
        fun onFileSuccess(imageFile: File) {}
        open fun onError(msg : String?=null) {}

    }


    /****************************UNSED */
    //Resize to avoid using too much memory loading big images (e.g.: 2560*1920)
    private fun getImageResized(context: Context, selectedImage: Uri): Bitmap? {
        var bm: Bitmap? = null

        try {
            val sampleSizes = intArrayOf(8, 5, 2, 1)
            var i = 0
            val minWidthQuality = 300
            do {
                bm = decodeBitmap(context, selectedImage, sampleSizes[i])

                d(TAG, "ReSizer: new bitmap width = " + bm!!.width)
                i++
            } while (bm!!.width < minWidthQuality && i < sampleSizes.size)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return bm
    }

    //decode bitmap and manipulate there sample
    private fun decodeBitmap(context: Context, theUri: Uri, sampleSize: Int): Bitmap? {

        val options = BitmapFactory.Options()
        options.inSampleSize = sampleSize

        var fileDescriptor: AssetFileDescriptor? = null
        try {
            fileDescriptor = context.contentResolver
                .openAssetFileDescriptor(
                    theUri,
                    "r"
                )
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        var actuallyUsableBitmap: Bitmap? = null
        if (fileDescriptor != null) {
            actuallyUsableBitmap = BitmapFactory.decodeFileDescriptor(
                fileDescriptor.fileDescriptor, null,
                options
            )

            d(
                TAG, options.inSampleSize.toString() + " sample method bitmap ... " +
                        actuallyUsableBitmap!!.width + " " + actuallyUsableBitmap.height
            )
        }

        return actuallyUsableBitmap
    }


    private fun checkRuntimePermission(activity: Activity, fragment: Fragment): Boolean {
        //check permission at runtime
        val checkSelfPermission = ContextCompat.checkSelfPermission(
            activity,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (checkSelfPermission != PackageManager.PERMISSION_GRANTED) {
            //Requests permissions to be granted to this application at runtime
            if(fragment!=null) {
                fragment.requestPermissions(
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            }
            else{
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            }
            return false
        }
        return true
    }

    @Throws(IOException::class)
    private fun decodeSampledBitmapFromResource(
        context: Context,
        uri: Uri,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap? {
        try {
            // First decode with inJustDecodeBounds=true to check dimensions
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

            // Decode bitmap with inSampleSize set

            options.inJustDecodeBounds = false
            val parcelFileDescriptor: ParcelFileDescriptor? = context.contentResolver.openFileDescriptor(
                uri,
                "r"
            )
            val fileDescriptor: FileDescriptor? = parcelFileDescriptor?.fileDescriptor
            val image: Bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options)
            parcelFileDescriptor?.close()
            return image
        } catch (e: Exception) {
            e.stackTrace
        }
        return null
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight = (height * .7).toInt()
            val halfWidth = (width * .7).toInt()

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    fun getCameraFilePath() = if(::mCurrentCameraPhotoPath.isInitialized) mCurrentCameraPhotoPath else null
    fun setCameraFilePath(path: String){ mCurrentCameraPhotoPath = path }

}
