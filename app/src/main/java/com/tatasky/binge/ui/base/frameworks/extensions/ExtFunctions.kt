package com.tatasky.binge.ui.base.frameworks.extensions

import android.content.Context
import android.os.Bundle
import android.text.InputFilter
import android.view.View
import android.view.ViewTreeObserver
import android.widget.AdapterView
import android.widget.Toast
import androidx.constraintlayout.widget.Group
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.tatasky.binge.customviews.MyGallery
import com.tatasky.binge.data.networking.models.response.LeftMenuItem
import com.tatasky.binge.utils.SingleClickListener
import com.tatasky.binge.utils.TYPE_GAMES

inline fun <reified T : Fragment> FragmentManager.findFragmentByClass(): T? =
    fragments.firstOrNull { it is T } as T?

fun TextInputEditText.setMaxLength(maxAllowedLength: Int = 1) {
    this.filters = arrayOf(InputFilter.LengthFilter(maxAllowedLength))
}

fun FragmentManager.getCurrentNavigationFragment(): Fragment? =
    primaryNavigationFragment?.childFragmentManager?.fragments?.first()

fun Context.toast(message: String, isLong: Boolean = false) {
    Toast.makeText(this, message, if (isLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
}

fun TextInputEditText.focus(){
    requestFocus()
    setSelection(length())
}

fun String?.takeIfNotBlankOrNull() : String? {
    return this?.takeIf { it.isNotBlank() }
}

fun List<LeftMenuItem>.getCategoriesItemList(): List<LeftMenuItem> {
    return this.filterIndexed { index, _ ->
        index > 3
    }
}

fun Group.setAllOnClickListener(listener: View.OnClickListener?) {
    referencedIds.forEach { id ->
        rootView.findViewById<View>(id).setOnClickListener(listener)
    }
}

/**
 * To disable multiple clicks on a view
 */
fun View.setSingleOnClick(
    clickIntervalMillis: Long = 500,
    doClick: (View) -> Unit
) =
    setOnClickListener(object :
        SingleClickListener(clickIntervalMillis) {
        override fun onClicked(v: View?) {
            if (v != null)
                doClick.invoke(v)
        }
    })

fun View.setSingleOnClickContentType(
    contentType: String? = null,
    clickIntervalMillis: Long = 500,
    doClick: (View) -> Unit
) {
    if (contentType.equals(TYPE_GAMES)) {
        setOnClickListener(object :
            SingleClickListener(clickIntervalMillis) {
            override fun onClicked(v: View?) {
                if (v != null)
                    doClick.invoke(v)
            }
        })
    } else {
        setOnClickListener { v ->
            if (v != null)
                doClick.invoke(v)
        }
    }
}

/*
 callback recyclerview extension when ui load finished
* */
fun RecyclerView.onUIComplete(action: () -> Unit) {
    val globalLayoutListener = object: ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            action()
            viewTreeObserver.removeOnGlobalLayoutListener(this)
        }
    }
    viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
}
inline fun <reified T> Map<String, T>.toBundle(): Bundle = bundleOf(*toList<String, T?>().toTypedArray())