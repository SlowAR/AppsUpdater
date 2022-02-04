package by.slowar.appsupdater.ui.updates_list.states.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar

fun animateShowProgress(progressView: ProgressBar, iconView: ImageView, durationMs: Long = 300) {
    progressView.animate().alpha(1f).setListener(object : AnimatorListenerAdapter() {
        override fun onAnimationStart(animation: Animator?) {
            super.onAnimationStart(animation)
            progressView.alpha = 0f
            progressView.visibility = View.VISIBLE
        }
    }).apply {
        duration = durationMs
    }

    iconView.animate().scaleX(0.5f).scaleY(0.5f).apply {
        duration = durationMs
    }
}

fun animateHideProgress(progressView: ProgressBar, iconView: ImageView, durationMs: Long = 300) {
    progressView.animate().alpha(0f).setListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator?) {
            super.onAnimationStart(animation)
            progressView.visibility = View.INVISIBLE
        }
    }).apply {
        duration = durationMs
    }

    iconView.animate().scaleX(1f).scaleY(1f).apply {
        duration = durationMs
    }
}