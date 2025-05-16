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
import kotlin.math.abs
import kotlin.random.Random

class DiagonalReadingTechnique : ReadingTechnique("Чтение по диагонали") {
    private var currentPosition = 0
    private var breakWordIndex = 0
    private var selectedTextIndex = 0
    private var fullText: String = ""
    private var animator: ValueAnimator? = null

    override val description: SpannableString
        get() {
            val text = "Чтение по диагонали — это техника скорочтения, при которой взгляд движется по диагональной линии от верхнего левого угла к нижнему правому, охватывая ключевые слова и фразы. Этот метод помогает быстро выделить основную информацию, не задерживаясь на каждом слове.\n" +
                    "Чтобы правильно применять эту методику, ведите взгляд по диагонали сверху вниз, не фокусируясь на каждом слове, а замечая ключевые смысловые точки текста.\n" +
                    "Внимание сосредотачивается на заголовках, терминах, цифрах и выделенных фрагментах, а второстепенные слова и детали игнорируются, что ускоряет процесс чтения и восприятия материала."
            val spannable = SpannableString(text)

            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), 0, name.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), text.indexOf("ведите взгляд по диагонали"), text.indexOf("ведите взгляд по диагонали") + "ведите взгляд по диагонали".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), text.indexOf("ключевые смысловые точки"), text.indexOf("ключевые смысловые точки") + "ключевые смысловые точки".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), text.indexOf("заголовках, терминах, цифрах и выделенных фрагментах"), text.indexOf("заголовках, терминах, цифрах и выделенных фрагментах") + "заголовках, терминах, цифрах и выделенных фрагментах".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

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
        breakWordIndex = 0

        textView.gravity = android.view.Gravity.TOP
        textView.post {
            showNextTextPart(textView, guideView)
        }
    }

    private fun showNextTextPart(
        textView: TextView,
        guideView: View
    ) {
        if (currentPosition >= fullText.length) {
            guideView.visibility = View.INVISIBLE
            Log.d("DiagonalReading", "Text ended, stopping animation")
            animator?.cancel()

            val currentText = textView.text.toString()
            textView.text = currentText
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

        val partText = fullText.substring(currentPosition, breakPosition).trim()
        Log.d("DiagonalReading", "Showing part: startPosition=$currentPosition, endPosition=$breakPosition, breakWord='$breakWord', text='$partText'")

        textView.text = partText
        startDiagonalAnimation(textView, guideView, breakPosition, partText)
    }

    private fun startDiagonalAnimation(textView: TextView, guideView: View, newPosition: Int, partText: String) {
        guideView.visibility = View.VISIBLE
        animator?.cancel()

        val wordCount = partText.split("\\s+".toRegex()).filter { it.isNotEmpty() }.size
        val durationPerWord = 40L
        val totalDuration = wordCount * durationPerWord

        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = totalDuration
            var lastLine = -1

            addUpdateListener { animation ->
                val fraction = animation.animatedValue as Float
                val width = textView.width.toFloat()
                val visibleHeight = textView.height.toFloat()

                val layout = textView.layout
                val totalLines = layout?.lineCount ?: 1
                val lastLineTop = if (totalLines > 1) layout.getLineTop(totalLines - 1) else visibleHeight
                val heightExcludingLastLine = if (totalLines > 1) lastLineTop.toFloat() else visibleHeight

                val y = fraction * heightExcludingLastLine
                val x = fraction * width

                guideView.translationX = x - (guideView.width / 2) + textView.left
                guideView.translationY = textView.top.toFloat()

                val currentLine = highlightWordAtPosition(textView, x, y, lastLine)
                if (currentLine != -1) lastLine = currentLine
            }
            addListener(
                onEnd = {
                    currentPosition = newPosition
                    breakWordIndex++
                    Log.d("DiagonalReading", "Animation ended, new currentPosition=$currentPosition, breakWordIndex=$breakWordIndex")
                    showNextTextPart(textView, guideView)
                }
            )
            start()
        }
    }

    private fun highlightWordAtPosition(textView: TextView, x: Float, y: Float, lastLine: Int): Int {
        val layout = textView.layout ?: return -1
        val visibleHeight = textView.height.toFloat()

        val adjustedY = y.coerceIn(0f, visibleHeight)
        val currentLine = layout.getLineForVertical(adjustedY.toInt())

        val totalLines = layout.lineCount
        if (currentLine == totalLines - 1) {
            return currentLine
        }

        if (currentLine <= lastLine) return currentLine

        val diagonalSlope = visibleHeight / textView.width.toFloat()
        val expectedX = adjustedY / diagonalSlope

        var closestOffset = -1
        var minDistance = Float.MAX_VALUE

        for (offset in layout.getLineStart(currentLine) until layout.getLineEnd(currentLine)) {
            if (textView.text[offset].isWhitespace()) continue

            val charLeft = layout.getPrimaryHorizontal(offset)
            val charRight = if (offset + 1 < textView.text.length) layout.getPrimaryHorizontal(offset + 1) else charLeft
            val charX = (charLeft + charRight) / 2

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
            spannable.removeSpan(BackgroundColorSpan(Color.YELLOW))
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
}