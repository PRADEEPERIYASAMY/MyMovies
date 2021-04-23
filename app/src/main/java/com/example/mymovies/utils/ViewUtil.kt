package com.example.mymovies.utils

import android.view.View
import androidx.annotation.DimenRes
import androidx.annotation.StringRes

fun View.getString(@StringRes resId: Int): String = resources.getString(resId)

fun View.getString(@StringRes resId: Int, vararg formatArgs: Any): String =
    resources.getString(resId, *formatArgs)

fun View.getDimenSize(@DimenRes resId: Int): Int =
    resources.getDimensionPixelSize(resId)

fun View.isVisible() = visibility == View.VISIBLE

fun View.isInvisible() = visibility == View.INVISIBLE

fun View.isGone() = visibility == View.GONE

fun View.toVisible() {
    visibility = View.VISIBLE
}

fun View.toInvisible() {
    visibility = View.INVISIBLE
}

fun View.toGone() {
    visibility = View.GONE
}