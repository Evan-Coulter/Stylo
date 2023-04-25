package com.coulter.stylo.util

import android.content.Context
import android.view.View
import android.view.animation.AnimationUtils
import com.coulter.stylo.R

internal fun fadeInView(context: Context, view: View, customDuration: Long? = null) {
    view.visibility = View.INVISIBLE
    val animation = AnimationUtils.loadAnimation(context, R.anim.fade_in)
    customDuration?.let { animation.duration = customDuration }
    view.startAnimation(animation)
    view.visibility = View.VISIBLE
}

internal fun fadeOutView(context: Context, view: View, customDuration: Long? = null) {
    view.visibility = View.VISIBLE
    val animation = AnimationUtils.loadAnimation(context, R.anim.fade_out)
    customDuration?.let { animation.duration = customDuration }
    view.startAnimation(animation)
    view.visibility = View.INVISIBLE
}
