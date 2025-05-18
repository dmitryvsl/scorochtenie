package com.example.scorochenie

import android.animation.ValueAnimator
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.animation.addListener
import kotlin.random.Random

class WordReverseTechnique : ReadingTechnique("Слова наоборот") {
    private var currentWordIndex = 0
    private var selectedTextIndex = 0
    private var fullText: String = ""
    private var currentPosition = 0
    private var breakWordIndex = 0
    private var currentPartText: String = ""
    private var animator: ValueAnimator? = null
    private var allWords: List<String> = emptyList()

    // Слова-прерыватели для перевёрнутого текста
    private val reverseBreakWords = listOf(
        listOf(", имыннёнартсорпсар ан йовон"),
        listOf(", как ет, еыроток", "юьтсорокс 000092("),
        listOf("в йиксечирткелэ лангис.")
    )

    override val description: SpannableString
        get() {
            val text = "Слова наоборот — это техника скорочтения, при которой буквы в словах читаются справа налево, но предложения — слева направо. Метод тренирует внимание и произвольность движения глаз.\n" +
                    "Для применения техники читайте предложения слева направо, переворачивая буквы каждого слова в уме.\n" +
                    "Сосредоточьтесь на разбиении слов на буквы и их правильной сборке, чтобы улучшить навыки чтения."
            val spannable = SpannableString(text)
            spannable.setSpan(android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, name.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(android.text.style.StyleSpan(android.graphics.Typeface.BOLD), text.indexOf("читайте предложения слева направо"), text.indexOf("читайте предложения слева направо") + "читайте предложения слева направо".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(android.text.style.StyleSpan(android.graphics.Typeface.BOLD), text.indexOf("переворачивая буквы"), text.indexOf("переворачивая буквы") + "переворачивая буквы".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            return spannable
        }

    override fun startAnimation(
        textView: TextView,
        guideView: View,
        onAnimationEnd: () -> Unit
    ) {
        selectedTextIndex = Random.nextInt(TextResources.sampleTexts.size)
        val originalText = TextResources.sampleTexts[selectedTextIndex]
        fullText = reverseWords(originalText).replace("\n", " ")
        currentPosition = 0
        breakWordIndex = 0
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
        if (currentPosition >= fullText.length) {
            guideView.visibility = View.INVISIBLE
            Log.d("WordReverse", "Text ended, stopping animation")
            animator?.cancel()
            val currentText = textView.text.toString()
            textView.text = currentText
            onAnimationEnd()
            return
        }

        val currentBreakWords = reverseBreakWords[selectedTextIndex]
        val breakWord = if (breakWordIndex < currentBreakWords.size) currentBreakWords[breakWordIndex] else ""
        val breakPosition = if (breakWord.isNotEmpty()) {
            val index = fullText.indexOf(breakWord, currentPosition)
            if (index == -1) {
                Log.e("WordReverse", "Break word '$breakWord' not found from position $currentPosition")
                fullText.length
            } else {
                index + breakWord.length
            }
        } else {
            fullText.length
        }

        currentPartText = fullText.substring(currentPosition, breakPosition).trim()
        allWords = currentPartText.split("\\s+".toRegex()).filter { it.isNotEmpty() }
        currentWordIndex = 0

        textView.text = currentPartText

        Log.d("WordReverse", "Showing part: startPosition=$currentPosition, endPosition=$breakPosition, breakWord='$breakWord', text='$currentPartText'")

        textView.post {
            animateNextWord(textView, guideView, onAnimationEnd)
        }
    }

    private fun reverseWords(text: String): String {
        val tokenRegex = Regex("""\w+|[^\s\w]""")
        val tokens = tokenRegex.findAll(text).map { it.value }.toList()

        val result = StringBuilder()
        var i = 0

        while (i < tokens.size) {
            val token = tokens[i]

            if (token.any { it.isLetterOrDigit() }) {
                // Это слово — собираем его + следующие за ним знаки препинания
                val word = token
                val punctuations = mutableListOf<String>()

                var j = i + 1
                while (j < tokens.size && !tokens[j].any { it.isLetterOrDigit() }) {
                    punctuations.add(tokens[j])
                    j++
                }

                // Переворачиваем само слово
                val reversedWord = word.reversed()

                // Добавляем в результат: перевёрнутое слово + пунктуация
                result.append(reversedWord)
                punctuations.forEach { result.append(it) }

                if (j < tokens.size) {
                    result.append(" ")
                }

                i = j
            } else {
                // Это не слово (например, запятая или точка), пропускаем
                i++
            }
        }

        return result.toString().trim()
    }

    private fun animateNextWord(
        textView: TextView,
        guideView: View,
        onAnimationEnd: () -> Unit
    ) {
        if (currentWordIndex >= allWords.size) {
            currentPosition += currentPartText.length + 1
            breakWordIndex++
            Log.d("WordReverse", "Part ended, moving to next part, new currentPosition=$currentPosition, breakWordIndex=$breakWordIndex")
            showNextTextPart(textView, guideView, onAnimationEnd)
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

        val (startIndex, word) = getWordPosition(currentWordIndex)
        if (startIndex >= 0 && startIndex < currentPartText.length) {
            val endIndex = startIndex + word.length
            spannable.setSpan(
                BackgroundColorSpan(Color.YELLOW),
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            Log.d("WordReverse", "Highlighting word: '$word', start=$startIndex, end=$endIndex")
        } else {
            Log.e("WordReverse", "Invalid word start index: $startIndex for word: '$word'")
        }

        textView.text = spannable
    }

    private fun getWordPosition(wordIndex: Int): Pair<Int, String> {
        var startIndex = 0
        var wordCount = 0
        allWords.forEachIndexed { index, word ->
            if (wordCount == wordIndex) {
                return Pair(startIndex, word)
            }
            startIndex += word.length
            if (startIndex < currentPartText.length && currentPartText[startIndex] == ' ') {
                startIndex++
            }
            wordCount++
        }
        return Pair(-1, "")
    }

    private fun startWordAnimation(
        textView: TextView,
        guideView: View,
        onAnimationEnd: () -> Unit
    ) {
        guideView.visibility = View.VISIBLE
        animator?.cancel()

        val layout = textView.layout ?: return
        val (wordStartIndex, word) = getWordPosition(currentWordIndex)
        if (wordStartIndex < 0 || wordStartIndex >= currentPartText.length) {
            Log.e("WordReverse", "Invalid wordStartIndex: $wordStartIndex for word: '$word'")
            currentWordIndex++
            animateNextWord(textView, guideView, onAnimationEnd)
            return
        }

        val wordEndIndex = wordStartIndex + word.length
        if (wordEndIndex > currentPartText.length) {
            Log.e("WordReverse", "Invalid wordEndIndex: $wordEndIndex for word: '$word'")
            currentWordIndex++
            animateNextWord(textView, guideView, onAnimationEnd)
            return
        }

        val startLine = layout.getLineForOffset(wordStartIndex)
        val endLine = layout.getLineForOffset(wordEndIndex)
        val startX = layout.getPrimaryHorizontal(wordStartIndex)
        val endX = layout.getPrimaryHorizontal(wordEndIndex)
        val lineY = layout.getLineTop(startLine).toFloat()

        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 50L
            addUpdateListener { animation ->
                val fraction = animation.animatedValue as Float
                val currentX = startX + (endX - startX) * fraction
                guideView.translationX = currentX - (guideView.width / 2) + textView.left
                guideView.translationY = lineY + textView.top.toFloat()
            }
            addListener(
                onEnd = {
                    currentWordIndex++
                    Log.d("WordReverse", "Word animation ended, currentWordIndex=$currentWordIndex")
                    animateNextWord(textView, guideView, onAnimationEnd)
                }
            )
            start()
        }
    }
}