package com.example.scorochenie

import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.View
import android.widget.TextView

class ReverseReadingTechnique : ReadingTechnique("Обратное чтение") {
    override val description: SpannableString
        get() {
            val text = "Обратное чтение — это техника скорочтения, при которой текст читается в обратном направлении, начиная с конца. Метод помогает лучше понять структуру текста и выделить ключевые идеи.\n" +
                    "Для применения техники начните с последнего абзаца и двигайтесь к началу, фиксируя основные выводы.\n" +
                    "Сосредоточьтесь на главных тезисах и связующих элементах, чтобы глубже осмыслить содержание."
            val spannable = SpannableString(text)
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), 0, name.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), text.indexOf("начните с последнего абзаца"), text.indexOf("начните с последнего абзаца") + "начните с последнего абзаца".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), text.indexOf("основные выводы"), text.indexOf("основные выводы") + "основные выводы".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), text.indexOf("главных тезисах"), text.indexOf("главных тезисах") + "главных тезисах".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            return spannable
        }

    override fun startAnimation(
        textView: TextView,
        guideView: View,
        onAnimationEnd: () -> Unit
    ) {
        textView.text = "Анимация для обратного чтения в разработке"
        guideView.visibility = View.INVISIBLE
        onAnimationEnd()
    }
}