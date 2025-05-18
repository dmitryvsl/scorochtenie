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

class KeywordSearchTechnique : ReadingTechnique("Поиск ключевых слов") {
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
            val text = "Поиск ключевых слов — это техника скорочтения, при которой читатель фокусируется только на наиболее значимых словах и фразах, игнорируя остальной текст. Этот метод позволяет быстро выделить суть материала.\n" +
                    "Для применения техники сканируйте текст, выделяя ключевые слова, такие как термины, имена или цифры.\n" +
                    "Пропускайте связующие слова и второстепенные детали, чтобы сосредоточиться на основном содержании и ускорить чтение."
            val spannable = SpannableString(text)
            spannable.setSpan(StyleSpan(Typeface.BOLD), 0, name.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(Typeface.BOLD), text.indexOf("сканируйте текст"), text.indexOf("сканируйте текст") + "сканируйте текст".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(Typeface.BOLD), text.indexOf("ключевые слова"), text.indexOf("ключевые слова") + "ключевые слова".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(Typeface.BOLD), text.indexOf("основном содержании"), text.indexOf("основном содержании") + "основном содержании".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
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
        lastScrollY = 0

        scrollView = textView.parent as? ScrollView
        Log.d("KeywordSearch", "ScrollView initialized: $scrollView, parent=${textView.parent}, parentClass=${textView.parent?.javaClass?.simpleName}")
        if (scrollView == null) {
            Log.e("KeywordSearch", "TextView is not inside a ScrollView, scrolling will not work")
        } else {
            Log.d("KeywordSearch", "ScrollView height: ${scrollView?.height}, width: ${scrollView?.width}")
        }

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

        Log.d("KeywordSearch", "Showing full text: '$currentPartText'")

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
            Log.d("KeywordSearch", "Text ended, stopping animation")
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
        Log.d("KeywordSearch", "Found keywords in text: ${foundKeyWords.joinToString(",")}")

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

        val layout = textView.layout
        if (layout == null) {
            Log.e("KeywordSearch", "TextView layout is null")
            textView.postDelayed({ animateNextWord(textView, guideView, onAnimationEnd) }, 200)
            return
        }

        val wordStartIndex = getWordStartIndex(currentWordIndex, currentPartText)
        val wordEndIndex = wordStartIndex + currentPartWords[currentWordIndex].length

        if (wordStartIndex < 0 || wordStartIndex >= currentPartText.length) {
            Log.e("KeywordSearch", "Invalid wordStartIndex: $wordStartIndex")
            currentWordIndex++
            animateNextWord(textView, guideView, onAnimationEnd)
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
                        Log.d("KeywordSearch", "Attempting scroll for line $startLine, word='${currentPartWords[currentWordIndex]}'")
                        Log.d("KeywordSearch", "Scroll parameters: line=$startLine, word='${currentPartWords[currentWordIndex]}', lineTop=$lineTopPosition, lineBottom=$lineBottomPosition, scrollViewHeight=$scrollViewHeight, currentScrollY=$currentScrollY, targetScrollY=$targetScrollY")
                        // Плавная прокрутка
                        ValueAnimator.ofInt(currentScrollY, targetScrollY).apply {
                            duration = 500L // Длительность анимации прокрутки
                            addUpdateListener { animation ->
                                val value = animation.animatedValue as Int
                                sv.scrollTo(0, value)
                            }
                            addListener(
                                onEnd = {
                                    lastScrollY = targetScrollY
                                    Log.d("KeywordSearch", "Scrolled to line $startLine, targetScrollY=$targetScrollY, currentScrollY=${sv.scrollY}")
                                }
                            )
                            start()
                        }
                    } else {
                        Log.d("KeywordSearch", "No scroll needed, already at target: line=$startLine, word='${currentPartWords[currentWordIndex]}', targetScrollY=$targetScrollY")
                    }
                } else {
                    Log.d("KeywordSearch", "No scroll needed, line $startLine is visible, lineTop=$lineTopPosition, lineBottom=$lineBottomPosition, visibleTop=$visibleTop, visibleBottom=$visibleBottom")
                }

                sv.postDelayed({
                    Log.d("KeywordSearch", "After scroll check, currentScrollY=${sv.scrollY}, textViewHeight=${textView.height}, scrollViewHeight=$scrollViewHeight")
                }, 100)
            }
        } ?: Log.e("KeywordSearch", "ScrollView is null, cannot scroll to line $startLine for word '${currentPartWords[currentWordIndex]}'")

        Log.d("KeywordSearch", "Animating word: '${currentPartWords[currentWordIndex]}' at position $currentWordIndex, startX=$startX, endX=$endX, lineY=$lineY, startLine=$startLine, endLine=$endLine")

        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 300L // Длительность анимации для одного слова
            addUpdateListener { animation ->
                val fraction = animation.animatedValue as Float
                val currentX = startX + (endX - startX) * fraction
                guideView.translationX = currentX - (guideView.width / 2) + textView.left
                guideView.translationY = lineY + textView.top.toFloat() - (scrollView?.scrollY?.toFloat() ?: 0f)
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