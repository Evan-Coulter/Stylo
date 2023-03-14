package com.example.stylo.util

import android.content.Context
import android.view.View
import android.view.animation.AnimationUtils
import com.example.stylo.R

internal fun fadeInView(context: Context, view: View) {
    view.visibility = View.INVISIBLE
    val animation = AnimationUtils.loadAnimation(context, R.anim.fade_in)
    view.startAnimation(animation)
    view.visibility = View.VISIBLE
}

internal fun fadeOutView(context: Context, view: View) {
    view.visibility = View.VISIBLE
    val animation = AnimationUtils.loadAnimation(context, R.anim.fade_out)
    view.startAnimation(animation)
    view.visibility = View.INVISIBLE
}
