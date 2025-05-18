package com.example.scorochenie

import android.animation.ValueAnimator
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.animation.addListener
import kotlin.random.Random
import android.graphics.Typeface

class PointerMethodTechnique : ReadingTechnique("Метод \"указки\"") {
    private var currentWordIndex = 0
    private var selectedTextIndex = 0
    private var fullText: String = ""
    private var animator: ValueAnimator? = null
    private var currentPartWords: List<String> = emptyList()
    private var currentPartText: String = ""

    override val description: SpannableString
        get() {
            val text = "Метод \"указки\" — это техника скорочтения, при которой используется палец, ручка или другой указатель для направления взгляда по тексту. Метод помогает поддерживать ритм чтения и избегать возвращений назад.\n" +
                    "Для применения техники ведите указку плавно вдоль строк, следуя за текстом.\n" +
                    "Контролируйте скорость движения указки, чтобы сосредоточиться на ключевых словах и ускорить восприятие информации."
            val spannable = SpannableString(text)
            spannable.setSpan(StyleSpan(Typeface.BOLD), 0, name.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(Typeface.BOLD), text.indexOf("ведите указку плавно"), text.indexOf("ведите указку плавно") + "ведите указку плавно".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(Typeface.BOLD), text.indexOf("ключевых словах"), text.indexOf("ключевых словах") + "ключевых словах".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            return spannable
        }

    override fun startAnimation(
        textView: TextView,
        guideView: View,
        onAnimationEnd: () -> Unit
    ) {
        selectedTextIndex = Random.nextInt(TextResources.sampleTexts.size)
        fullText = TextResources.sampleTexts[selectedTextIndex].replace("\n", " ")
        currentWordIndex = 0

        textView.gravity = android.view.Gravity.TOP
        textView.isSingleLine = false
        textView.maxLines = Int.MAX_VALUE
        textView.post {
            showNextTextPart(textView, guideView, onAnimationEnd)
        }
    }

    private fun showNextTextPart(
        textView: TextView,
        guideView: View,
        onAnimationEnd: () -> Unit
    ) {
        currentPartText = fullText
        currentPartWords = currentPartText.split("\\s+".toRegex()).filter { it.isNotEmpty() }
        currentWordIndex = 0

        Log.d("PointerMethod", "Showing full text: '$currentPartText'")

        textView.text = currentPartText
        animateNextWord(textView, guideView, onAnimationEnd)
    }

    private fun animateNextWord(
        textView: TextView,
        guideView: View,
        onAnimationEnd: () -> Unit
    ) {
        if (currentWordIndex >= currentPartWords.size) {
            guideView.visibility = View.INVISIBLE
            Log.d("PointerMethod", "Text ended, stopping animation")
            animator?.cancel()
            textView.text = currentPartText
            onAnimationEnd()
            return
        }

        highlightWord(textView)
        startWordAnimation(textView, guideView, onAnimationEnd)
    }

    private fun highlightWord(textView: TextView) {
        val spannable = SpannableString(currentPartText)
        val existingSpans = spannable.getSpans(0, spannable.length, BackgroundColorSpan::class.java)
        for (span in existingSpans) {
            spannable.removeSpan(span)
        }

        var startIndex = 0
        var wordCount = 0

        currentPartWords.forEach { word ->
            if (wordCount == currentWordIndex) {
                val endIndex = startIndex + word.length
                spannable.setSpan(
                    BackgroundColorSpan(Color.YELLOW),
                    startIndex,
                    endIndex,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                Log.d("PointerMethod", "Highlighting word: '$word', start=$startIndex, end=$endIndex")
            }
            startIndex += word.length
            if (startIndex < currentPartText.length && currentPartText[startIndex] == ' ') {
                startIndex++ // Пропускаем пробел
            }
            wordCount++
        }

        textView.text = spannable
    }

    private fun startWordAnimation(
        textView: TextView,
        guideView: View,
        onAnimationEnd: () -> Unit
    ) {
        guideView.visibility = View.VISIBLE
        animator?.cancel()

        val layout = textView.layout ?: return
        val wordStartIndex = getWordStartIndex(currentWordIndex, currentPartText)
        val wordEndIndex = wordStartIndex + currentPartWords[currentWordIndex].length

        val startLine = layout.getLineForOffset(wordStartIndex)
        val endLine = layout.getLineForOffset(wordEndIndex)

        val startX = layout.getPrimaryHorizontal(wordStartIndex)
        val endX = layout.getPrimaryHorizontal(wordEndIndex)
        val lineY = layout.getLineTop(startLine).toFloat()

        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 300L
            addUpdateListener { animation ->
                val fraction = animation.animatedValue as Float
                val currentX = startX + (endX - startX) * fraction

                guideView.translationX = currentX - (guideView.width / 2) + textView.left
                guideView.translationY = lineY + textView.top.toFloat()
            }
            addListener(
                onEnd = {
                    currentWordIndex++
                    Log.d("PointerMethod", "Word animation ended, currentWordIndex=$currentWordIndex")
                    animateNextWord(textView, guideView, onAnimationEnd)
                }
            )
            start()
        }
    }

    private fun getWordStartIndex(wordIndex: Int, text: String): Int {
        var startIndex = 0
        var count = 0
        text.split("\\s+".toRegex()).forEachIndexed { index, word ->
            if (count == wordIndex) {
                return startIndex
            }
            startIndex += word.length
            if (startIndex < text.length && text[startIndex] == ' ') {
                startIndex++ // Пропускаем пробел
            }
            count++
        }
        return startIndex
    }
}