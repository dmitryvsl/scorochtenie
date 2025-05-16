package com.example.scorochenie

import android.text.SpannableString
import android.view.View
import android.widget.TextView

abstract class ReadingTechnique(val name: String) {
    open val description: SpannableString
        get() = SpannableString("Описание техники в разработке")

    abstract fun startAnimation(
        textView: TextView,
        guideView: View,
        onAnimationEnd: () -> Unit
    )
}