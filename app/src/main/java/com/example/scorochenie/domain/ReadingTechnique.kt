package com.example.scorochenie.domain

import android.text.SpannableString
import android.view.View
import android.widget.TextView

abstract class ReadingTechnique(name: String, displayName: String) : Technique(name, displayName) {
    abstract override val description: SpannableString

    abstract override fun startAnimation(
        textView: TextView,
        guideView: View,
        durationPerWord: Long,
        selectedTextIndex: Int,
        onAnimationEnd: () -> Unit
    )

    override fun cancelAnimation() {
        // Пустая реализация по умолчанию для техник без анимации
    }
}