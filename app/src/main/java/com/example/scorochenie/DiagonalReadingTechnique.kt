package com.example.scorochenie

import android.animation.ValueAnimator
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
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
            val text = "Чтение по диагонали — это способ быстрого чтения, при котором взгляд скользит по диагональной линии от верхнего левого угла к нижнему правому. В процессе внимания уделяется основным смысловым элементам — такими как заголовки, числа и важные фразы — без подробной проработки каждого слова. Такой подход позволяет быстро уловить суть прочитанного.\n" +
                    "Чтобы правильно применять эту методику, ведите взгляд по диагонали сверху вниз, не фокусируясь на каждом слове, а замечая ключевые смысловые точки текста."
            val spannable = SpannableString(text)

            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), 0, name.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), text.indexOf("ведите взгляд по диагонали"), text.indexOf("ведите взгляд по диагонали") + "ведите взгляд по диагонали".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), text.indexOf("ключевые смысловые точки"), text.indexOf("ключевые смысловые точки") + "ключевые смысловые точки".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            return spannable
        }

    override fun startAnimation(
        textView: TextView,
        guideView: View,
        durationPerWord: Long,
        onAnimationEnd: () -> Unit
    ) {
        selectedTextIndex = Random.nextInt(TextResources.sampleTexts.size)
        fullText = TextResources.sampleTexts[selectedTextIndex].replace("\n", " ")
        currentPosition = 0
        breakWordIndex = 0

        val wordDurationMs = (60_000 / durationPerWord).coerceAtLeast(50L)
        Log.d("DiagonalReading", "Starting animation with durationPerWord=$durationPerWord WPM, wordDurationMs=$wordDurationMs ms")

        guideView.visibility = View.INVISIBLE
        Log.d("DiagonalReading", "startAnimation: guideView visibility=${guideView.visibility} (0=INVISIBLE, 8=VISIBLE)")

        textView.gravity = android.view.Gravity.TOP
        textView.isSingleLine = false
        textView.maxLines = Int.MAX_VALUE

        textView.post {
            Log.d("DiagonalReading", "TextView size after post: ${textView.width}x${textView.height}")
            val parent = textView.parent as View
            Log.d("DiagonalReading", "FrameLayout size after text set: ${parent.width}x${parent.height}")
            showNextTextPart(textView, guideView, wordDurationMs, onAnimationEnd)
        }
    }

    private fun showNextTextPart(
        textView: TextView,
        guideView: View,
        wordDurationMs: Long,
        onAnimationEnd: () -> Unit
    ) {
        if (currentPosition >= fullText.length) {
            guideView.visibility = View.INVISIBLE
            Log.d("DiagonalReading", "Text ended, stopping animation, guideView visibility=${guideView.visibility}")
            animator?.cancel()
            clearHighlight(textView)
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

        val partText = fullText.substring(currentPosition, breakPosition).trim()
        Log.d("DiagonalReading", "Showing part: startPosition=$currentPosition, endPosition=$breakPosition, breakWord='$breakWord', text='$partText'")

        textView.text = partText
        textView.visibility = View.VISIBLE

        textView.post {
            Log.d("DiagonalReading", "TextView size after text set: ${textView.width}x${textView.height}")
            val parent = textView.parent as View
            Log.d("DiagonalReading", "FrameLayout size after text set: ${parent.width}x${parent.height}")
            val diagonalLineView = parent.findViewById<DiagonalLineView>(R.id.diagonal_line_view)
            if (diagonalLineView != null) {
                diagonalLineView.requestLayout()
                Log.d("DiagonalReading", "DiagonalLineView found, visibility=${diagonalLineView.visibility}")
                startDiagonalAnimation(textView, guideView, breakPosition, partText, wordDurationMs, onAnimationEnd)
            } else {
                Log.e("DiagonalReading", "DiagonalLineView not found, skipping animation")
                onAnimationEnd()
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
        animator?.cancel()

        val wordCount = partText.split("\\s+".toRegex()).filter { it.isNotEmpty() }.size
        val totalDuration = wordCount * wordDurationMs

        Log.d("DiagonalReading", "Animating part with wordCount=$wordCount, wordDurationMs=$wordDurationMs, totalDuration=$totalDuration ms")

        val layout = textView.layout
        if (layout == null) {
            Log.e("DiagonalReading", "TextView layout is null, retrying")
            textView.requestLayout()
            textView.postDelayed({ startDiagonalAnimation(textView, guideView, newPosition, partText, wordDurationMs, onAnimationEnd) }, 50)
            return
        }

        val width = textView.width.toFloat()
        val visibleHeight = textView.height.toFloat()
        val totalLines = layout.lineCount
        val lastLineTop = if (totalLines > 1) layout.getLineTop(totalLines - 1) else visibleHeight
        val heightExcludingLastLine = if (totalLines > 1) lastLineTop.toFloat() else visibleHeight

        guideView.visibility = View.VISIBLE
        guideView.translationX = 0f
        guideView.translationY = 0f
        Log.d("DiagonalReading", "Initial guideView position: x=${guideView.translationX}, y=${guideView.translationY}, visibility=${guideView.visibility}")

        val initialLine = highlightWordAtPosition(textView, 0f, 0f, -1)
        Log.d("DiagonalReading", "Initial highlight called, currentLine=$initialLine")

        var lastTime = System.currentTimeMillis()
        var lastX = 0f
        var lastY = 0f

        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = totalDuration
            interpolator = LinearInterpolator() // Гарантируем линейное изменение
            var lastLine = initialLine

            addUpdateListener { animation ->
                val fraction = animation.animatedValue as Float
                val y = fraction * heightExcludingLastLine
                val x = fraction * width

                guideView.translationX = x - (guideView.width / 2)
                guideView.translationY = y

                val currentTime = System.currentTimeMillis()
                val deltaTime = (currentTime - lastTime) / 1000f
                if (deltaTime > 0) {
                    val speedX = (x - lastX) / deltaTime
                    val speedY = (y - lastY) / deltaTime
                    Log.d("DiagonalReading", "guideView position: x=$x, y=$y, fraction=$fraction, visibility=${guideView.visibility}, speedX=$speedX px/s, speedY=$speedY px/s")
                }
                lastTime = currentTime
                lastX = x
                lastY = y

                val currentLine = highlightWordAtPosition(textView, x, y, lastLine)
                if (currentLine != -1) lastLine = currentLine
            }
            addListener(
                onEnd = {
                    clearHighlight(textView)
                    guideView.visibility = View.INVISIBLE
                    Log.d("DiagonalReading", "Animation ended, guideView visibility=${guideView.visibility}")
                    currentPosition = newPosition
                    breakWordIndex++
                    Log.d("DiagonalReading", "Animation ended, new currentPosition=$currentPosition, breakWordIndex=$breakWordIndex")
                    showNextTextPart(textView, guideView, wordDurationMs, onAnimationEnd)
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
            Log.d("DiagonalReading", "Highlighted word: start=$start, end=$end, text='${text.substring(start, end)}'")
        }

        return currentLine
    }

    private fun clearHighlight(textView: TextView) {
        val text = textView.text.toString()
        val spannable = SpannableString(text)
        val existingSpans = spannable.getSpans(0, spannable.length, BackgroundColorSpan::class.java)
        for (span in existingSpans) {
            spannable.removeSpan(span)
        }
        textView.text = spannable
        Log.d("DiagonalReading", "Cleared highlight from text")
    }
}