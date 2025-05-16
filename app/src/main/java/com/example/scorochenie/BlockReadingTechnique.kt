package com.example.scorochenie

import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.View
import android.widget.TextView

class BlockReadingTechnique : ReadingTechnique("Чтение \"блоками\"") {
    override val description: SpannableString
        get() {
            val text = "Чтение \"блоками\" — это техника скорочтения, при которой текст воспринимается целыми смысловыми блоками, а не отдельными словами. Метод ускоряет обработку информации за счет группировки слов в логические единицы.\n" +
                    "Чтобы применять эту технику, захватывайте взглядом группы слов одновременно, объединяя их в смысловые фрагменты.\n" +
                    "Сосредоточьтесь на целостных идеях, чтобы быстрее уловить суть текста без лишнего чтения."
            val spannable = SpannableString(text)
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), 0, name.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), text.indexOf("захватывайте взглядом группы слов"), text.indexOf("захватывайте взглядом группы слов") + "захватывайте взглядом группы слов".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), text.indexOf("смысловые фрагменты"), text.indexOf("смысловые фрагменты") + "смысловые фрагменты".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), text.indexOf("целостных идеях"), text.indexOf("целостных идеях") + "целостных идеях".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            return spannable
        }

    override fun startAnimation(
        textView: TextView,
        guideView: View,
        onAnimationEnd: () -> Unit
    ) {
        textView.text = "Анимация для чтения блоками в разработке"
        guideView.visibility = View.INVISIBLE
        onAnimationEnd()
    }
}