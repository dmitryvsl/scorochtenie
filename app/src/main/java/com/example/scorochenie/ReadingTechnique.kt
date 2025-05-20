package com.example.scorochenie

import android.text.SpannableString
import android.view.View
import android.widget.TextView

abstract class ReadingTechnique(val name: String) {
    open val description: SpannableString = SpannableString("")

    abstract fun startAnimation(
        textView: TextView,
        guideView: View,
        durationPerWord: Long,
        selectedTextIndex: Int,  // новый параметр
        onAnimationEnd: () -> Unit
    )
    open fun cancelAnimation() {
        // Пустая реализация по умолчанию для техник без анимации
    }
}
