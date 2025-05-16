package com.example.scorochenie

import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.View
import android.widget.TextView

class PointerMethodTechnique : ReadingTechnique("Метод \"указки\"") {
    override val description: SpannableString
        get() {
            val text = "Метод \"указки\" — это техника скорочтения, при которой используется палец, ручка или другой указатель для направления взгляда по тексту. Метод помогает поддерживать ритм чтения и избегать возвращений назад.\n" +
                    "Для применения техники ведите указку плавно вдоль строк, следуя за текстом.\n" +
                    "Контролируйте скорость движения указки, чтобы сосредоточиться на ключевых словах и ускорить восприятие информации."
            val spannable = SpannableString(text)
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), 0, name.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), text.indexOf("ведите указку плавно"), text.indexOf("ведите указку плавно") + "ведите указку плавно".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), text.indexOf("ключевых словах"), text.indexOf("ключевых словах") + "ключевых словах".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            return spannable
        }

    override fun startAnimation(
        textView: TextView,
        guideView: View,
        onAnimationEnd: () -> Unit
    ) {
        textView.text = "Анимация для метода указки в разработке"
        guideView.visibility = View.INVISIBLE
        onAnimationEnd()
    }
}