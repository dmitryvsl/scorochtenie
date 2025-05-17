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

class ReverseReadingTechnique : ReadingTechnique("Обратное чтение") {
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
        selectedTextIndex = Random.nextInt(TextResources.sampleTexts.size)
        val originalText = TextResources.sampleTexts[selectedTextIndex]
        // Инвертируем порядок слов в каждой строке
        fullText = reverseWordsInLines(originalText).replace("\n", " ")
        currentPosition = 0
        currentWordIndex = 0
        breakWordIndex = 0

        textView.gravity = android.view.Gravity.TOP
        textView.post {
            showNextTextPart(textView, guideView, onAnimationEnd)
        }
    }

    private fun reverseWordsInLines(text: String): String {
        // Проверяем наличие \n
        val hasNewlines = text.contains("\n")
        val lines = if (hasNewlines) {
            text.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        } else {
            // Разбиваем на строки по 5 слов, если \n нет
            val words = text.split("\\s+".toRegex()).filter { it.isNotEmpty() }
            words.chunked(5).map { it.joinToString(" ") }
        }

        // Инвертируем слова в каждой строке
        val reversedLines = lines.map { line ->
            line.split("\\s+".toRegex()).filter { it.isNotEmpty() }.reversed().joinToString(" ")
        }

        // Собираем строки с \n
        return reversedLines.joinToString("\n")
    }

    private fun showNextTextPart(
        textView: TextView,
        guideView: View,
        onAnimationEnd: () -> Unit
    ) {
        if (currentPosition >= fullText.length) {
            guideView.visibility = View.INVISIBLE
            Log.d("ReverseReading", "Text ended, stopping animation")
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

        Log.d("ReverseReading", "Showing part: startPosition=$currentPosition, endPosition=$breakPosition, breakWord='$breakWord', text='$currentPartText'")

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
            Log.d("ReverseReading", "Part ended, moving to next part, new currentPosition=$currentPosition, breakWordIndex=$breakWordIndex")
            showNextTextPart(textView, guideView, onAnimationEnd)
            return
        }

        highlightWord(textView)
        startWordAnimation(textView, guideView, onAnimationEnd)
    }

    private fun highlightWord(textView: TextView) {
        val spannable = SpannableString(currentPartText)
        // Удаляем все существующие BackgroundColorSpan
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
                Log.d("ReverseReading", "Highlighting word: '$word', start=$startIndex, end=$endIndex")
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
                    Log.d("ReverseReading", "Word animation ended, currentWordIndex=$currentWordIndex")
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