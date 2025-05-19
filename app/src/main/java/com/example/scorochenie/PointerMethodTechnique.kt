package com.example.scorochenie

import android.animation.ValueAnimator
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.widget.ScrollView
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
    private var scrollView: ScrollView? = null
    private var lastScrollY: Int = 0

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
        durationPerWord: Long,
        selectedTextIndex: Int,
        onAnimationEnd: () -> Unit
    ) {
        this.selectedTextIndex = selectedTextIndex
        fullText = TextResources.sampleTexts[selectedTextIndex].replace("\n", " ")
        currentWordIndex = 0
        lastScrollY = 0

        // Преобразуем WPM в миллисекунды на слово
        val wordDurationMs = (60_000 / durationPerWord).coerceAtLeast(50L) // Минимум 50 мс
        Log.d("PointerMethod", "Starting animation with durationPerWord=$durationPerWord WPM, wordDurationMs=$wordDurationMs ms")

        scrollView = textView.parent as? ScrollView
        Log.d("PointerMethod", "ScrollView initialized: $scrollView, parent=${textView.parent}, parentClass=${textView.parent?.javaClass?.simpleName}")
        if (scrollView == null) {
            Log.e("PointerMethod", "TextView is not inside a ScrollView, scrolling will not work")
        } else {
            Log.d("PointerMethod", "ScrollView height: ${scrollView?.height}, width: ${scrollView?.width}")
        }

        textView.gravity = android.view.Gravity.TOP
        textView.isSingleLine = false
        textView.maxLines = Int.MAX_VALUE
        textView.post {
            showNextTextPart(textView, guideView, wordDurationMs, onAnimationEnd)
        }
    }

    private fun showNextTextPart(
        textView: TextView,
        guideView: View,
        wordDurationMs: Long,
        onAnimationEnd: () -> Unit
    ) {
        currentPartText = fullText
        currentPartWords = currentPartText.split("\\s+".toRegex()).filter { it.isNotEmpty() }
        currentWordIndex = 0

        Log.d("PointerMethod", "Showing full text: '$currentPartText', wordCount=${currentPartWords.size}")

        textView.text = currentPartText
        animateNextWord(textView, guideView, wordDurationMs, onAnimationEnd)
    }

    private fun animateNextWord(
        textView: TextView,
        guideView: View,
        wordDurationMs: Long,
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
        startWordAnimation(textView, guideView, wordDurationMs, onAnimationEnd)
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
        wordDurationMs: Long,
        onAnimationEnd: () -> Unit
    ) {
        guideView.visibility = View.INVISIBLE
        animator?.cancel()

        val layout = textView.layout
        if (layout == null) {
            Log.e("PointerMethod", "TextView layout is null")
            textView.postDelayed({ animateNextWord(textView, guideView, wordDurationMs, onAnimationEnd) }, 200)
            return
        }

        val wordStartIndex = getWordStartIndex(currentWordIndex, currentPartText)
        val wordEndIndex = wordStartIndex + currentPartWords[currentWordIndex].length

        if (wordStartIndex < 0 || wordStartIndex >= currentPartText.length) {
            Log.e("PointerMethod", "Invalid wordStartIndex: $wordStartIndex")
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
        val lineY = (lineTop + lineBottom) / 2 // Середина строки для guideView

        // Прокрутка ScrollView
        scrollView?.let { sv ->
            sv.post {
                val scrollViewHeight = sv.height
                val currentScrollY = sv.scrollY
                val lineTopPosition = layout.getLineTop(startLine)
                val lineBottomPosition = layout.getLineBottom(startLine)

                // Определяем видимую область (верхняя треть экрана)
                val visibleTop = currentScrollY
                val visibleBottom = currentScrollY + scrollViewHeight * 2 / 3

                // Прокручиваем, если строка не полностью видна
                if (lineTopPosition < visibleTop || lineBottomPosition > visibleBottom) {
                    // Цель: поставить строку в верхнюю треть экрана
                    val targetScrollY = (lineTopPosition - scrollViewHeight / 3).coerceAtLeast(0).toInt()
                    if (targetScrollY != lastScrollY) {
                        Log.d("PointerMethod", "Attempting scroll for line $startLine, word='${currentPartWords[currentWordIndex]}'")
                        Log.d("PointerMethod", "Scroll parameters: line=$startLine, word='${currentPartWords[currentWordIndex]}', lineTop=$lineTopPosition, lineBottom=$lineBottomPosition, scrollViewHeight=$scrollViewHeight, currentScrollY=$currentScrollY, targetScrollY=$targetScrollY")
                        // Плавная прокрутка
                        ValueAnimator.ofInt(currentScrollY, targetScrollY).apply {
                            duration = wordDurationMs / 2 // Прокрутка быстрее анимации слова
                            addUpdateListener { animation ->
                                val value = animation.animatedValue as Int
                                sv.scrollTo(0, value)
                            }
                            addListener(
                                onEnd = {
                                    lastScrollY = targetScrollY
                                    Log.d("PointerMethod", "Scrolled to line $startLine, targetScrollY=$targetScrollY, currentScrollY=${sv.scrollY}")
                                }
                            )
                            start()
                        }
                    } else {
                        Log.d("PointerMethod", "No scroll needed, already at target: line=$startLine, word='${currentPartWords[currentWordIndex]}', targetScrollY=$targetScrollY")
                    }
                } else {
                    Log.d("PointerMethod", "No scroll needed, line $startLine is visible, lineTop=$lineTopPosition, lineBottom=$lineBottomPosition, visibleTop=$visibleTop, visibleBottom=$visibleBottom")
                }

                sv.postDelayed({
                    Log.d("PointerMethod", "After scroll check, currentScrollY=${sv.scrollY}, textViewHeight=${textView.height}, scrollViewHeight=$scrollViewHeight")
                }, 100)
            }
        } ?: Log.e("PointerMethod", "ScrollView is null, cannot scroll to line $startLine for word '${currentPartWords[currentWordIndex]}'")

        Log.d("PointerMethod", "Animating word: '${currentPartWords[currentWordIndex]}' at position $currentWordIndex, startX=$startX, endX=$endX, lineY=$lineY, startLine=$startLine, endLine=$endLine, duration=$wordDurationMs ms")

        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = wordDurationMs
            addUpdateListener { animation ->
                val fraction = animation.animatedValue as Float
                val currentX = startX + (endX - startX) * fraction
                guideView.translationX = currentX - (guideView.width / 2) + textView.left
                guideView.translationY = lineY + textView.top.toFloat() - (scrollView?.scrollY?.toFloat() ?: 0f)
            }
            addListener(
                onEnd = {
                    currentWordIndex++
                    Log.d("PointerMethod", "Word animation ended, currentWordIndex=$currentWordIndex")
                    animateNextWord(textView, guideView, wordDurationMs, onAnimationEnd)
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