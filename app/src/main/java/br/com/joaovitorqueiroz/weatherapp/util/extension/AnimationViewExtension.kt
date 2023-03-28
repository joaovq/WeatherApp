package br.com.joaovitorqueiroz.weatherapp.util.extension

import android.content.Context
import android.view.View
import android.view.animation.AnimationUtils
import androidx.annotation.AnimRes
import androidx.appcompat.R
import java.time.Duration

fun View.startAnimationFromId(
    context: Context,
    @AnimRes animationId: Int = R.anim.abc_slide_in_bottom,
    duration: Long = 2000
) {
    val slideOutBottom = AnimationUtils.loadAnimation(context, animationId)
    slideOutBottom.duration = duration
    this.startAnimation(slideOutBottom)
}
