package com.tatasky.binge.helper

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.tatasky.binge.R
import com.tatasky.binge.utils.dpToPx
import com.tatasky.binge.utils.e
import com.tatasky.binge.utils.getCloudinaryUrl
import com.tatasky.binge.utils.isTablet


fun Context.isValidGlideContext() = this !is Activity || (!this.isDestroyed && !this.isFinishing)


fun imageLoad(img: ImageView, url: String) {
    try {
        e("GlideHelper1","url : $url")
        if(img.context.isValidGlideContext()) {
            Glide.with(img.context)
                .setDefaultRequestOptions(
                    RequestOptions()
                        .placeholder(R.drawable.shp_placeholder)
                )/*Use to remove image from background*/
                .load(url)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        img.post {
                            Glide.with(img.context)
                                .setDefaultRequestOptions(
                                    RequestOptions()
                                        .placeholder(R.drawable.shp_placeholder)
                                )/*Use to remove image from background*/
                                .load(url)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                .dontTransform()
                                .into(img)
                        }
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
//                        img.setImageDrawable(resource)
                        return false;
                    }
                })
                .override(img.width, img.height)
                .transition(DrawableTransitionOptions.withCrossFade())//in milliseconds
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .dontTransform()
                .into(img)
        }
    } catch (e: Exception) {
        com.tatasky.binge.utils.e("Error", e.message)
    }
}

fun loadImageTextViewDrawable(textView: TextView, url: String,cloudinaryUrl:String?) {
    if(textView.context.isValidGlideContext()){
        try {
            var sizeOfImage=120
            if(isTablet(textView.context)){
                sizeOfImage = 100
            }
            val url = getCloudinaryUrl(cloudinaryUrl,url)
            Glide.with(textView.context)
                .load(url)
                .placeholder(R.drawable.shp_placeholder)
                .error(R.drawable.shp_placeholder)
                .into(object : CustomTarget<Drawable>(sizeOfImage,sizeOfImage) {
                    override fun onLoadCleared(drawable: Drawable?) {
                        textView.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null)
                    }

                    override fun onResourceReady(
                        res: Drawable,
                        transition: com.bumptech.glide.request.transition.Transition<in Drawable>?
                    ) {
                        textView.setCompoundDrawablesWithIntrinsicBounds(null, res, null, null)
                    }

                })
        } catch (e: Exception) {
            com.tatasky.binge.utils.e("Error", e.message)
        }
    }
}


fun transparentImageLoad(img: ImageView, url: String) {
    e("transparentImageLoad","url : $url")
    if(img.context.isValidGlideContext()){
        try {
            Glide.with(img.context)
                .setDefaultRequestOptions(
                    RequestOptions()
                        .placeholder(R.color.transparent)
                )/*Use to remove image from background*/
                .load(url)
                .override(img.width,img.height)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .dontTransform()
                .into(img)
        } catch (e: Exception) {
            com.tatasky.binge.utils.e("Error", e.message)
        }
    }
}

fun circularImageLoad(img: ImageView, url: String) {
    if(img.context.isValidGlideContext()) {
        e("GlideHelper2","url : $url")
        Glide.with(img.context)
            .load(url)
            .placeholder(R.drawable.ic_profile_settings)
            .error(R.drawable.ic_profile_settings)
            .apply(RequestOptions.circleCropTransform())
//            .transition(DrawableTransitionOptions.withCrossFade(100))//in milliseconds
            .into(img)
    }
}
fun circularImageLoadWithoutCache(img: ImageView, url: String, @DrawableRes bg : Int){
    if(img.context.isValidGlideContext()) {
        e("GlideHelper3","url : $url")
        Glide.with(img.context)
            .load(url)
            .placeholder(bg)
            .apply(RequestOptions.circleCropTransform())
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
//        .skipMemoryCache(true)
            //.signature(ObjectKey(System.currentTimeMillis()))
            .transition(DrawableTransitionOptions.withCrossFade())//in milliseconds
            .into(img)
    }
}

fun circularBitmapImageLoad(img: ImageView, bitmap: String){

    try{
        if(img.context.isValidGlideContext()) {
            Glide.with(img.context)
                .asBitmap()
                .load(bitmap)
                .apply(RequestOptions.circleCropTransform())
                .into(img)
        }
    } catch (e: Exception) {
        com.tatasky.binge.utils.e("Error", e.message)
    }
}
fun circularImageApps(img: ImageView, url: String){
    if(img.context.isValidGlideContext()) {
        Glide.with(img.context).load(url)
            .placeholder(R.drawable.white_circle)/*Use to remove image from background*/
            .apply(RequestOptions.circleCropTransform())
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    img.post {
                        Glide.with(img.context)
                            .setDefaultRequestOptions(
                                RequestOptions()
                                    .placeholder(R.drawable.white_circle)
                            )/*Use to remove image from background*/
                            .load(url)
                            .apply(RequestOptions.circleCropTransform())
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            .into(img)
                    }
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
//                    img.setImageDrawable(resource)
                    return false
                }
            })
            .into(img)
    }
}


fun imageLoadRounded(img: ImageView, url: String, round: Int, @DrawableRes placeHolder: Int) {
    if(img.context.isValidGlideContext())
    {
        try {
            e("GlideHelper4","url : $url")
            Glide.with(img.context)
                .setDefaultRequestOptions(
                    RequestOptions()
                        .placeholder(placeHolder)
                )
                .load(url)
                .apply(RequestOptions.circleCropTransform())
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
//                .transition(DrawableTransitionOptions.withCrossFade())//in milliseconds
                .into(img)
            /*Glide.with(img.context)
                .setDefaultRequestOptions(
                    RequestOptions()
                        .placeholder(placeHolder)
                )*//*Use to remove image from background*//*
                .load(url)
                .transform(RoundedCorners(round))
                .override(img.width, img.height)
                .transition(DrawableTransitionOptions.withCrossFade())//in milliseconds
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
//                .dontTransform()
//                .apply(RequestOptions().transform(RoundedCorners(round)))
                .into(img)*/
        } catch (e: Exception) {
            com.tatasky.binge.utils.e("Error", e.message)
        }
    }
}


fun imageLoadWithPlaceHolder(img: ImageView, url: String, @DrawableRes placeHolder: Int) {
    if (img.context.isValidGlideContext()) {
        try {
            e("GlideHelper5","url : $url")
            Glide.with(img.context)
                .setDefaultRequestOptions(
                    RequestOptions()
                        .placeholder(placeHolder)
                )/*Use to remove image from background*/
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(img)
        } catch (e: Exception) {
            e("Error", e.message)
        }
    }
}

fun circularImageLocal(img: ImageView, @DrawableRes drawableId: Int, @DrawableRes placeHolder: Int){
    if(img.context.isValidGlideContext()) {
        Glide.with(img.context)
            .load(drawableId)
            .placeholder(placeHolder)
            .apply(RequestOptions.circleCropTransform())
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .transition(DrawableTransitionOptions.withCrossFade())//in milliseconds
            .into(img)
    }
}


fun loadSubscriptionBottomSheetBanner(img: ImageView, url: String) {
    val x = dpToPx(img.context,20).toFloat()
    var requestOptions= RequestOptions().transform(CenterInside(),GranularRoundedCorners(x, x, 0F, 0F))
    if(isTablet(img.context)){
        requestOptions = RequestOptions().transform(CenterInside(),GranularRoundedCorners(x, x, x, x))
    }
    Glide.with(img.context)
        .setDefaultRequestOptions(
           requestOptions
        ).load(url).into(img)
}
