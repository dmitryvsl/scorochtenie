package com.example.scorochenie

import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.View
import android.widget.TextView

class PeripheralReadingTechnique : ReadingTechnique("Периферийное чтение") {
    override val description: SpannableString
        get() {
            val text = "Периферийное чтение — это техника скорочтения, при которой используется боковое зрение для охвата большего объема текста за один взгляд. Метод позволяет сократить количество движений глаз.\n" +
                    "Для применения техники расширяйте поле зрения, захватывая слова на периферии.\n" +
                    "Фокусируйтесь на центральных словах, но используйте боковое зрение для восприятия соседних слов, чтобы ускорить чтение."
            val spannable = SpannableString(text)
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), 0, name.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), text.indexOf("расширяйте поле зрения"), text.indexOf("расширяйте поле зрения") + "расширяйте поле зрения".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), text.indexOf("боковое зрение"), text.indexOf("боковое зрение") + "боковое зрение".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), text.indexOf("восприятия соседних слов"), text.indexOf("восприятия соседних слов") + "восприятия соседних слов".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            return spannable
        }

    override fun startAnimation(
        textView: TextView,
        guideView: View,
        onAnimationEnd: () -> Unit
    ) {
        textView.text = "Анимация для периферийного чтения в разработке"
        guideView.visibility = View.INVISIBLE
        onAnimationEnd()
    }
}