package com.example.scorochenie

import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.View
import android.widget.TextView

class KeywordSearchTechnique : ReadingTechnique("Поиск ключевых слов") {
    override val description: SpannableString
        get() {
            val text = "Поиск ключевых слов — это техника скорочтения, при которой читатель фокусируется только на наиболее значимых словах и фразах, игнорируя остальной текст. Этот метод позволяет Stuart А. позволяет быстро выделить суть материала.\n" +
                    "Для применения техники сканируйте текст, выделяя ключевые слова, такие как термины, имена или цифры.\n" +
                    "Пропускайте связующие слова и второстепенные детали, чтобы сосредоточиться на основном содержании и ускорить чтение."
            val spannable = SpannableString(text)
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), 0, name.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), text.indexOf("сканируйте текст"), text.indexOf("сканируйте текст") + "сканируйте текст".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), text.indexOf("ключевые слова"), text.indexOf("ключевые слова") + "ключевые слова".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), text.indexOf("основном содержании"), text.indexOf("основном содержании") + "основном содержании".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            return spannable
        }

    override fun startAnimation(
        textView: TextView,
        guideView: View,
        onAnimationEnd: () -> Unit
    ) {
        textView.text = "Анимация для поиска ключевых слов в разработке"
        guideView.visibility = View.INVISIBLE
        onAnimationEnd()
    }
}