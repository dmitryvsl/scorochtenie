package com.example.scorochenie

import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.View
import android.widget.TextView

class VerticalReadingTechnique : ReadingTechnique("Вертикальное чтение") {
    override val description: SpannableString
        get() {
            val text = "Вертикальное чтение — это техника скорочтения, при которой взгляд движется сверху вниз по центру страницы, охватывая основные слова в каждой строке. Метод помогает быстро обработать текст, минимизируя горизонтальные движения глаз.\n" +
                    "Чтобы использовать эту технику, ведите взгляд строго вертикально, фиксируя центральные слова строк.\n" +
                    "Сосредоточьтесь на ключевых терминах и фразах, игнорируя второстепенные слова, чтобы ускорить восприятие информации."
            val spannable = SpannableString(text)
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), 0, name.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), text.indexOf("ведите взгляд строго вертикально"), text.indexOf("ведите взгляд строго вертикально") + "ведите взгляд строго вертикально".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), text.indexOf("центральные слова строк"), text.indexOf("центральные слова строк") + "центральные слова строк".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), text.indexOf("ключевых терминах и фразах"), text.indexOf("ключевых терминах и фразах") + "ключевых терминах и фразах".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            return spannable
        }

    override fun startAnimation(
        textView: TextView,
        guideView: View,
        onAnimationEnd: () -> Unit
    ) {
        textView.text = "Анимация для вертикального чтения в разработке"
        guideView.visibility = View.INVISIBLE
        onAnimationEnd()
    }
}