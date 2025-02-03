package com.github.ParagonVirtuoso.memorias.util

import android.view.View
import androidx.core.content.ContextCompat
import com.github.ParagonVirtuoso.memorias.R
import com.google.android.material.snackbar.Snackbar

fun View.showSuccessSnackbar(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
    Snackbar.make(this, message, duration).apply {
        setBackgroundTint(ContextCompat.getColor(context, R.color.success_color))
        setTextColor(ContextCompat.getColor(context, R.color.white))
        animationMode = Snackbar.ANIMATION_MODE_SLIDE
        setAction("OK") { dismiss() }
        setActionTextColor(ContextCompat.getColor(context, R.color.white))
    }.show()
}

fun View.showErrorSnackbar(message: String, duration: Int = Snackbar.LENGTH_LONG) {
    Snackbar.make(this, message, duration).apply {
        setBackgroundTint(ContextCompat.getColor(context, R.color.error_color))
        setTextColor(ContextCompat.getColor(context, R.color.white))
        animationMode = Snackbar.ANIMATION_MODE_SLIDE
        setAction("OK") { dismiss() }
        setActionTextColor(ContextCompat.getColor(context, R.color.white))
    }.show()
}

fun View.showInfoSnackbar(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
    Snackbar.make(this, message, duration).apply {
        setBackgroundTint(ContextCompat.getColor(context, R.color.info_color))
        setTextColor(ContextCompat.getColor(context, R.color.white))
        animationMode = Snackbar.ANIMATION_MODE_SLIDE
        setAction("OK") { dismiss() }
        setActionTextColor(ContextCompat.getColor(context, R.color.white))
    }.show()
} 