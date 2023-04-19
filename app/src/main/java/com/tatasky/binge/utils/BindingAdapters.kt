package com.tatasky.binge.utils

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Html
import android.text.SpannedString
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.tatasky.binge.R
import com.tatasky.binge.helper.circularImageLoad
import com.tatasky.binge.helper.imageLoad
import com.tatasky.binge.ui.base.frameworks.extensions.disable
import com.tatasky.binge.ui.base.frameworks.extensions.hide
import com.tatasky.binge.ui.base.frameworks.extensions.show


/**
 * Created by Srikant Karnani on 10/09/19.
 */
class BindingAdapters {
    companion object {

        /**
         * Custom binding adapter to set bottom margin of a view
         * Use app:layout_marginBottom="value" namespace
         **/
        @JvmStatic
        @BindingAdapter("layout_marginBottom")
        fun setLayoutMarginBottom(view: View, dimen: Float) {
            val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.bottomMargin = dimen.toInt()
            view.layoutParams = layoutParams
        }

        @JvmStatic
        @BindingAdapter("glideSrc")
        fun ImageView.setGlideSrc(url: String?) {
            if (url != null) {
                imageLoad(this, url)
            }
        }

        @JvmStatic
        @BindingAdapter("glideSrc")
        fun ImageView.setGlideSrc(drawable: Drawable?) {
            if (drawable != null)
                setImageDrawable(drawable)
        }

        @JvmStatic
        @BindingAdapter("glideSrc")
        fun ImageView.setGlideSrc(resource: Int?) {
            if (resource != null)
                setImageResource(resource)
        }

        @JvmStatic
        @BindingAdapter("glideCircleSrc")
        fun ImageView.setGlideCircleSrc(url: String?) {
            circularImageLoad(this, url!!)
        }

        @JvmStatic
        @BindingAdapter("setAdapter")
        fun RecyclerView.bindRecyclerViewAdapter(adapter: RecyclerView.Adapter<*>?) {
            if (adapter != null)
                this.adapter = adapter
        }

        @JvmStatic
        @BindingAdapter("htmlText")
        fun TextView.setHtmlText(text: String?) {
            text?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    setText(Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT))
                } else {
                    setText(Html.fromHtml(text))
                }
            }
        }


        @JvmStatic
        @BindingAdapter("setPremiumGradient")
        fun TextView.setPremiumGradient(b : Boolean){
            if(b){
                paintPremiumGradient(this, this.paint.measureText(this.text.toString()))
            }
        }


        @JvmStatic
        @BindingAdapter("disable")
        fun SeekBar.setDisabled(enabled: Boolean) {
            this.disable()
            this.alpha = 1f
            this.progressDrawable.alpha = 255
        }

        @JvmStatic
        @BindingAdapter("startAvd")
        fun ImageView.startAvd(enabled: Boolean) {
            if (enabled) {
                show()
                if (drawable is AnimatedVectorDrawable)
                    (drawable as AnimatedVectorDrawable).start()
            } else {
                hide()
            }
        }

        @JvmStatic
        @BindingAdapter("required", "hint")
        fun TextView.setText(required: Boolean, hint: String) {
            if (required) {
                this.text = buildSpannedString {
                    append(hint)
                    color(
                        ContextCompat.getColor(
                            context,
                            R.color.darkError
                        )
                    ) { append(" *") }
                }
            } else {
                this.text = hint
            }
        }

        @JvmStatic
        @BindingAdapter("underlined", "text")
        fun MaterialButton.setText(required: Boolean, text: String) {
            this.text = text
            if (required) {
                this.paintFlags = Paint.UNDERLINE_TEXT_FLAG
            }
        }

        @JvmStatic
        @BindingAdapter("spannedText")
        fun TextView.setSpannedText(spannedString: SpannedString?) {
            this.text = spannedString
        }

        @JvmStatic
        @BindingAdapter("textColor", "defaultTextColor")
        fun TextView.textColor(color: String, @ColorInt defaultColor: Int) {
            try {
                this.setTextColor(Color.parseColor(color))
            } catch (e: Exception) {
                this.setTextColor(defaultColor)
            }
        }

        @JvmStatic
        @BindingAdapter("backgroundTint", "defaultBackgroundTint")
        fun View.backgroundTint(color: String, @ColorInt defaultColor: Int) {
            try {
                this.backgroundTintList = ColorStateList.valueOf(Color.parseColor(color))
            } catch (e: Exception) {
                this.backgroundTintList = ColorStateList.valueOf(defaultColor)
            }
        }

        @JvmStatic
        @BindingAdapter("strikeThrough")
        fun TextView.strikeThrough(strikeThrough: Boolean) {
            if (strikeThrough) {
                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                paintFlags = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
        }

        @JvmStatic
        @BindingAdapter("increaseTouch")
        fun increaseTouch(view: View, value: Float) {
            val parent = view.parent
            (parent as View).post {
                val rect = Rect()
                view.getHitRect(rect)
                val intValue = value.toInt()
                rect.top -= intValue    // increase top hit area
                rect.left -= intValue   // increase left hit area
                rect.bottom += intValue // increase bottom hit area
                rect.right += intValue  // increase right hit area
                parent.touchDelegate = TouchDelegate(rect, view)
            }
        }
    }
}
