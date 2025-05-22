package com.example.scorochenie.domain

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.view.View
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.animation.addListener
import android.text.style.StyleSpan
class WordReverseTechnique : ReadingTechnique("WordReverseTechnique", "Слова наоборот") {
    private var currentWordIndex = 0
    private var selectedTextIndex = 0
    private var fullText: String = ""
    private var currentPartText: String = ""
    private var animator: ValueAnimator? = null
    private var allWords: List<String> = emptyList()
    private var scrollView: ScrollView? = null
    private var lastScrollY: Int = 0
    private val handler = Handler(Looper.getMainLooper())
    private var isAnimationActive = false

    override val description: SpannableString
        get() {
            val text = "Слова наоборот — это техника скорочтения, при которой буквы в словах читаются справа налево, но предложения — слева направо. Метод тренирует внимание и произвольность движения глаз.\n" +
                    "Для применения техники читайте предложения слева направо, переворачивая буквы каждого слова в уме.\n" +
                    "Сосредоточьтесь на разбиении слов на буквы и их правильной сборке, чтобы улучшить навыки чтения."
            val spannable = SpannableString(text)
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), 0, name.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), text.indexOf("читайте предложения слева направо"), text.indexOf("читайте предложения слева направо") + "читайте предложения слева направо".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), text.indexOf("переворачивая буквы"), text.indexOf("переворачивая буквы") + "переворачивая буквы".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            return spannable
        }

    override fun startAnimation(
        textView: TextView,
        guideView: View,
        durationPerWord: Long,
        selectedTextIndex: Int,
        onAnimationEnd: () -> Unit
    ) {
        this.selectedTextIndex = selectedTextIndex
        val originalText = TextResources.otherTexts["Слова наоборот"]?.getOrNull(selectedTextIndex)?.text?.replace("\n", " ") ?: ""
        fullText = reverseWords(originalText).replace("\n", " ")
        currentWordIndex = 0
        lastScrollY = 0
        isAnimationActive = true

        val safeDurationPerWord = if (durationPerWord <= 0) 400L else durationPerWord
        val wordDurationMs = (60_000 / safeDurationPerWord).coerceAtLeast(50L)

        scrollView = textView.parent as? ScrollView

        textView.gravity = android.view.Gravity.TOP
        textView.isSingleLine = false
        textView.maxLines = Int.MAX_VALUE
        handler.post {
            if (isAnimationActive) {
                showNextTextPart(textView, guideView, wordDurationMs, onAnimationEnd)
            }
        }
    }

    private fun showNextTextPart(
        textView: TextView,
        guideView: View,
        wordDurationMs: Long,
        onAnimationEnd: () -> Unit
    ) {
        if (!isAnimationActive) return

        currentPartText = fullText
        allWords = currentPartText.split("\\s+".toRegex()).filter { it.isNotEmpty() }
        currentWordIndex = 0

        textView.text = currentPartText

        handler.post {
            if (isAnimationActive) {
                animateNextWord(textView, guideView, wordDurationMs, onAnimationEnd)
            }
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
                val word = token
                val punctuations = mutableListOf<String>()

                var j = i + 1
                while (j < tokens.size && !tokens[j].any { it.isLetterOrDigit() }) {
                    punctuations.add(tokens[j])
                    j++
                }

                val reversedWord = word.reversed()
                result.append(reversedWord)
                punctuations.forEach { result.append(it) }

                if (j < tokens.size) {
                    result.append(" ")
                }

                i = j
            } else {
                i++
            }
        }

        return result.toString().trim()
    }

    private fun animateNextWord(
        textView: TextView,
        guideView: View,
        wordDurationMs: Long,
        onAnimationEnd: () -> Unit
    ) {
        if (!isAnimationActive) return

        if (currentWordIndex >= allWords.size) {
            guideView.visibility = View.INVISIBLE
            animator?.cancel()
            textView.text = currentPartText
            if (isAnimationActive) onAnimationEnd()
            return
        }

        highlightWord(textView)
        startWordAnimation(textView, guideView, wordDurationMs, onAnimationEnd)
    }

    private fun highlightWord(textView: TextView) {
        if (!isAnimationActive) return

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
        wordDurationMs: Long,
        onAnimationEnd: () -> Unit
    ) {
        if (!isAnimationActive) return

        guideView.visibility = View.INVISIBLE
        animator?.cancel()

        val layout = textView.layout
        if (layout == null) {
            handler.postDelayed({
                if (isAnimationActive) animateNextWord(textView, guideView, wordDurationMs, onAnimationEnd)
            }, 200)
            return
        }

        val (wordStartIndex, word) = getWordPosition(currentWordIndex)
        if (wordStartIndex < 0 || wordStartIndex >= currentPartText.length) {
            currentWordIndex++
            animateNextWord(textView, guideView, wordDurationMs, onAnimationEnd)
            return
        }

        val wordEndIndex = wordStartIndex + word.length
        if (wordEndIndex > currentPartText.length) {
            currentWordIndex++
            animateNextWord(textView, guideView, wordDurationMs, onAnimationEnd)
            return
        }

        val startLine = layout.getLineForOffset(wordStartIndex)
        val endLine = layout.getLineForOffset(wordEndIndex)
        val startX = layout.getPrimaryHorizontal(wordStartIndex)
        var endX = layout.getPrimaryHorizontal(wordEndIndex)
        if (endX == startX) {
            endX = startX + layout.getPrimaryHorizontal(wordStartIndex + 1)
        }
        val lineTop = layout.getLineTop(startLine).toFloat()
        val lineBottom = layout.getLineBottom(startLine).toFloat()
        val lineY = (lineTop + lineBottom) / 2

        scrollView?.let { sv ->
            handler.post {
                if (!isAnimationActive) return@post
                val scrollViewHeight = sv.height
                val currentScrollY = sv.scrollY
                val lineTopPosition = layout.getLineTop(startLine)
                val lineBottomPosition = layout.getLineBottom(startLine)

                val visibleTop = currentScrollY
                val visibleBottom = currentScrollY + scrollViewHeight * 2 / 3

                if (lineTopPosition < visibleTop || lineBottomPosition > visibleBottom) {
                    val targetScrollY = (lineTopPosition - scrollViewHeight / 3).coerceAtLeast(0).toInt()
                    if (targetScrollY != lastScrollY) {
                        ValueAnimator.ofInt(currentScrollY, targetScrollY).apply {
                            duration = wordDurationMs / 2
                            addUpdateListener { animation ->
                                val value = animation.animatedValue as Int
                                sv.scrollTo(0, value)
                            }
                            addListener(
                                onEnd = {
                                    lastScrollY = targetScrollY
                                }
                            )
                            start()
                        }
                    }
                }
            }
        }

        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = wordDurationMs
            addUpdateListener { animation ->
                if (!isAnimationActive) return@addUpdateListener
                val fraction = animation.animatedValue as Float
                val currentX = startX + (endX - startX) * fraction
                guideView.translationX = currentX - (guideView.width / 2) + textView.left
                guideView.translationY = lineY + textView.top.toFloat() - (scrollView?.scrollY?.toFloat() ?: 0f)
            }
            addListener(
                onEnd = {
                    if (isAnimationActive) {
                        currentWordIndex++
                        animateNextWord(textView, guideView, wordDurationMs, onAnimationEnd)
                    }
                }
            )
            start()
        }
    }

    override fun cancelAnimation() {
        isAnimationActive = false
        animator?.cancel()
        handler.removeCallbacksAndMessages(null)
    }
}