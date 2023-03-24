package br.com.joaovitorqueiroz.weatherapp.util.extension

import android.content.Context
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.R

fun View.startAnimationDefault(context: Context) {
    val slideOutBottom = AnimationUtils.loadAnimation(context, R.anim.abc_slide_in_bottom)
    slideOutBottom.duration = 2000
    this.startAnimation(slideOutBottom)
}
