package com.example.scorochenie.domain

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.TextView
import androidx.core.animation.addListener
import com.example.scorochenie.ui.DiagonalLineView
import com.example.scorochenie.R
import kotlin.math.abs
import android.text.style.StyleSpan

class DiagonalReadingTechnique : Technique("DiagonalReadingTechnique", "Чтение по диагонали") {
    private var currentPosition = 0
    private var breakWordIndex = 0
    private var selectedTextIndex = 0
    private var fullText: String = ""
    private var animator: ValueAnimator? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isAnimationActive = false

    override val description: SpannableString
        get() {
            val text = "Чтение по диагонали — это способ быстрого ознакомления с текстом, при котором взгляд скользит сверху вниз по диагонали, захватывая общую структуру и главные элементы.\n" +
                    "Вместо того чтобы читать каждое слово, вы охватываете страницу бегло, выхватывая смысловые опоры — такие как начальные и конечные слова абзацев, цифры или повторы.\n" +
                    "Этот метод позволяет быстро получить общее представление о содержании и решить, стоит ли читать подробнее."
            val spannable = SpannableString(text)
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), 0, name.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), text.indexOf("сверху вниз по диагонали"), text.indexOf("сверху вниз по диагонали") + "сверху вниз по диагонали".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), text.indexOf("смысловые опоры"), text.indexOf("смысловые опоры") + "смысловые опоры".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), text.indexOf("начальные и конечные"), text.indexOf("начальные и конечные") + "начальные и конечные".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
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
        fullText = TextResources.getDiagonalTexts().getOrNull(selectedTextIndex)?.text?.replace("\n", " ") ?: ""
        currentPosition = 0
        breakWordIndex = 0
        isAnimationActive = true

        val safeDurationPerWord = if (durationPerWord <= 0) 400L else durationPerWord
        val wordDurationMs = (60_000 / safeDurationPerWord).coerceAtLeast(50L)

        guideView.visibility = View.INVISIBLE

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

        if (currentPosition >= fullText.length) {
            guideView.visibility = View.INVISIBLE
            animator?.cancel()
            clearHighlight(textView)
            if (isAnimationActive) onAnimationEnd()
            return
        }

        val currentBreakWords = TextResources.getDiagonalTexts().getOrNull(selectedTextIndex)?.breakWords ?: emptyList()
        val breakWord = if (breakWordIndex < currentBreakWords.size) currentBreakWords[breakWordIndex] else ""
        val breakPosition = if (breakWord.isNotEmpty()) {
            val index = fullText.indexOf(breakWord, currentPosition)
            if (index == -1) fullText.length else index + breakWord.length
        } else {
            fullText.length
        }

        val partText = fullText.substring(currentPosition, breakPosition).trim()

        textView.text = partText
        textView.visibility = View.VISIBLE

        handler.post {
            if (!isAnimationActive) return@post
            val parent = textView.parent as View
            val diagonalLineView = parent.findViewById<DiagonalLineView>(R.id.diagonal_line_view)
            if (diagonalLineView != null) {
                diagonalLineView.requestLayout()
                startDiagonalAnimation(textView, guideView, breakPosition, partText, wordDurationMs, onAnimationEnd)
            } else {
                if (isAnimationActive) onAnimationEnd()
            }
        }
    }

    private fun startDiagonalAnimation(
        textView: TextView,
        guideView: View,
        newPosition: Int,
        partText: String,
        wordDurationMs: Long,
        onAnimationEnd: () -> Unit
    ) {
        if (!isAnimationActive) return

        animator?.cancel()

        val wordCount = partText.split("\\s+".toRegex()).filter { it.isNotEmpty() }.size
        val totalDuration = wordCount * wordDurationMs

        val layout = textView.layout
        if (layout == null) {
            handler.postDelayed({
                if (isAnimationActive) startDiagonalAnimation(textView, guideView, newPosition, partText, wordDurationMs, onAnimationEnd)
            }, 50)
            return
        }

        val width = textView.width.toFloat()
        val visibleHeight = textView.height.toFloat()
        val totalLines = layout.lineCount
        val lastLineTop = if (totalLines > 1) layout.getLineTop(totalLines - 1) else visibleHeight
        val heightExcludingLastLine = if (totalLines > 1) lastLineTop.toFloat() else visibleHeight

        guideView.visibility = View.INVISIBLE
        guideView.translationX = 0f
        guideView.translationY = 0f

        val initialLine = highlightWordAtPosition(textView, 0f, 0f, -1)

        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = totalDuration
            interpolator = LinearInterpolator()
            var lastLine = initialLine

            addUpdateListener { animation ->
                if (!isAnimationActive) return@addUpdateListener
                val fraction = animation.animatedValue as Float
                val y = fraction * heightExcludingLastLine
                val x = fraction * width

                guideView.translationX = x - (guideView.width / 2)
                guideView.translationY = y

                val currentLine = highlightWordAtPosition(textView, x, y, lastLine)
                if (currentLine != -1) lastLine = currentLine
            }
            addListener(
                onEnd = {
                    if (!isAnimationActive) return@addListener
                    clearHighlight(textView)
                    guideView.visibility = View.INVISIBLE
                    currentPosition = newPosition
                    breakWordIndex++
                    showNextTextPart(textView, guideView, wordDurationMs, onAnimationEnd)
                }
            )
            start()
        }
    }

    private fun highlightWordAtPosition(textView: TextView, x: Float, y: Float, lastLine: Int): Int {
        if (!isAnimationActive) return -1

        val layout = textView.layout ?: return -1
        val visibleHeight = textView.height.toFloat()

        val adjustedY = y.coerceIn(0f, visibleHeight)
        val currentLine = layout.getLineForVertical(adjustedY.toInt())

        val totalLines = layout.lineCount
        if (currentLine == totalLines - 1 || currentLine <= lastLine) {
            return currentLine
        }

        val diagonalSlope = visibleHeight / textView.width.toFloat()
        val expectedX = adjustedY / diagonalSlope

        var closestOffset = -1
        var minDistance = Float.MAX_VALUE

        for (offset in layout.getLineStart(currentLine) until layout.getLineEnd(currentLine)) {
            if (textView.text[offset].isWhitespace()) continue

            val charLeft = layout.getPrimaryHorizontal(offset)
            val charRight = if (offset + 1 < textView.text.length) layout.getPrimaryHorizontal(offset + 1) else charLeft
            var charX = (charLeft + charRight) / 2

            val distance = abs(charX - expectedX)
            if (distance < minDistance) {
                minDistance = distance
                closestOffset = offset
            }
        }

        if (closestOffset != -1) {
            val text = textView.text.toString()
            var start = closestOffset
            var end = closestOffset

            while (start > 0 && !text[start - 1].isWhitespace()) start--
            while (end < text.length && !text[end].isWhitespace()) end++

            val spannable = SpannableString(text)
            val existingSpans = spannable.getSpans(0, spannable.length, BackgroundColorSpan::class.java)
            for (span in existingSpans) {
                spannable.removeSpan(span)
            }
            spannable.setSpan(
                BackgroundColorSpan(Color.YELLOW),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            textView.text = spannable
        }

        return currentLine
    }

    private fun clearHighlight(textView: TextView) {
        if (!isAnimationActive) return

        val text = textView.text.toString()
        val spannable = SpannableString(text)
        val existingSpans = spannable.getSpans(0, spannable.length, BackgroundColorSpan::class.java)
        for (span in existingSpans) {
            spannable.removeSpan(span)
        }
        textView.text = spannable
    }

    override fun cancelAnimation() {
        isAnimationActive = false
        animator?.cancel()
        handler.removeCallbacksAndMessages(null)
    }
}