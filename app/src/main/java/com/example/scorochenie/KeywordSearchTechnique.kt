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

class KeywordSearchTechnique : ReadingTechnique("Поиск ключевых слов") {
    private var currentWordIndex = 0
    private var selectedTextIndex = 0
    private var fullText: String = ""
    private var currentPosition = 0
    private var breakWordIndex = 0
    private var animator: ValueAnimator? = null
    private var currentPartWords: List<String> = emptyList()
    private var currentPartText: String = ""

    override val description: SpannableString
        get() {
            val text = "Поиск ключевых слов — это техника скорочтения, при которой читатель фокусируется только на наиболее значимых словах и фразах, игнорируя остальной текст. Этот метод позволяет быстро выделить суть материала.\n" +
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
        selectedTextIndex = Random.nextInt(TextResources.sampleTexts.size)
        fullText = TextResources.sampleTexts[selectedTextIndex].replace("\n", " ")
        currentPosition = 0
        currentWordIndex = 0
        breakWordIndex = 0

        textView.gravity = android.view.Gravity.TOP
        textView.post {
            showNextTextPart(textView, guideView, onAnimationEnd)
        }
    }

    private fun showNextTextPart(
        textView: TextView,
        guideView: View,
        onAnimationEnd: () -> Unit
    ) {
        if (currentPosition >= fullText.length) {
            guideView.visibility = View.INVISIBLE
            Log.d("KeywordSearch", "Text ended, stopping animation")
            animator?.cancel()
            // Сохраняем текущий текст, как в DiagonalReadingTechnique
            val currentText = textView.text.toString()
            textView.text = currentText
            onAnimationEnd()
            return
        }

        val currentBreakWords = TextResources.breakWords[selectedTextIndex]
        val breakWord = if (breakWordIndex < currentBreakWords.size) currentBreakWords[breakWordIndex] else ""
        val breakPosition = if (breakWord.isNotEmpty()) {
            val index = fullText.indexOf(breakWord, currentPosition)
            if (index == -1) fullText.length else index + breakWord.length
        } else {
            fullText.length
        }

        currentPartText = fullText.substring(currentPosition, breakPosition).trim()
        currentPartWords = currentPartText.split("\\s+".toRegex()).filter { it.isNotEmpty() }
        currentWordIndex = 0

        Log.d("KeywordSearch", "Showing part: startPosition=$currentPosition, endPosition=$breakPosition, breakWord='$breakWord', text='$currentPartText'")

        textView.text = currentPartText
        animateNextWord(textView, guideView, onAnimationEnd)
    }

    private fun animateNextWord(
        textView: TextView,
        guideView: View,
        onAnimationEnd: () -> Unit
    ) {
        if (currentWordIndex >= currentPartWords.size) {
            currentPosition += currentPartText.length + 1
            breakWordIndex++
            Log.d("KeywordSearch", "Part ended, moving to next part, new currentPosition=$currentPosition, breakWordIndex=$breakWordIndex")
            showNextTextPart(textView, guideView, onAnimationEnd)
            return
        }

        highlightWord(textView)
        startWordAnimation(textView, guideView, onAnimationEnd)
    }

    private fun highlightWord(textView: TextView) {
        val spannable = SpannableString(currentPartText)

        // Удаляем все существующие BackgroundColorSpan, StyleSpan и ForegroundColorSpan
        val existingBackgroundSpans = spannable.getSpans(0, spannable.length, BackgroundColorSpan::class.java)
        for (span in existingBackgroundSpans) {
            spannable.removeSpan(span)
        }
        val existingStyleSpans = spannable.getSpans(0, spannable.length, StyleSpan::class.java)
        for (span in existingStyleSpans) {
            spannable.removeSpan(span)
        }
        val existingForegroundSpans = spannable.getSpans(0, spannable.length, android.text.style.ForegroundColorSpan::class.java)
        for (span in existingForegroundSpans) {
            spannable.removeSpan(span)
        }

        // Выделяем ключевые слова жирным шрифтом и красным цветом
        val keyWords = TextResources.keyWords[selectedTextIndex]
        val foundKeyWords = mutableListOf<String>()
        keyWords.forEach { keyWord ->
            var startIndex = currentPartText.indexOf(keyWord, ignoreCase = false)
            while (startIndex != -1) {
                val endIndex = startIndex + keyWord.length
                spannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    startIndex,
                    endIndex,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannable.setSpan(
                    android.text.style.ForegroundColorSpan(Color.RED),
                    startIndex,
                    endIndex,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                foundKeyWords.add(keyWord)
                startIndex = currentPartText.indexOf(keyWord, startIndex + 1, ignoreCase = false)
            }
        }
        Log.d("KeywordSearch", "Found keywords in part: ${foundKeyWords.joinToString(",")}")

        // Подсвечиваем текущее слово жёлтым фоном
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
                Log.d("KeywordSearch", "Highlighting word: '$word', start=$startIndex, end=$endIndex")
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
            duration = 300L // Длительность анимации для одного слова
            addUpdateListener { animation ->
                val fraction = animation.animatedValue as Float
                val currentX = startX + (endX - startX) * fraction

                guideView.translationX = currentX - (guideView.width / 2) + textView.left
                guideView.translationY = lineY + textView.top.toFloat()
            }
            addListener(
                onEnd = {
                    currentWordIndex++
                    Log.d("KeywordSearch", "Word animation ended, currentWordIndex=$currentWordIndex")
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