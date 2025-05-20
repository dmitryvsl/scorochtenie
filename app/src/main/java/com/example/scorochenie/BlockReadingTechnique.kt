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
import kotlin.math.min

class BlockReadingTechnique : ReadingTechnique("Чтение \"блоками\"") {
    private var currentBlockIndex = 0
    private var selectedTextIndex = 0
    private var fullText: String = ""
    private var animator: ValueAnimator? = null
    private var currentPartText: String = ""
    private var lineCount: Int = 0
    private var lines: List<IntRange> = emptyList()
    private var scrollView: ScrollView? = null
    private var lastScrollY: Int = 0

    override val description: SpannableString
        get() {
            val text = "Чтение \"блоками\" — это техника скорочтения, при которой текст воспринимается не по отдельным словам, а целыми смысловыми фрагментами. Такой подход помогает быстрее обрабатывать информацию и лучше удерживать общий контекст.\n" +
                    "Сосредоточьтесь на восприятии сразу нескольких строк как единого блока — это развивает навык охватывать больше текста за раз и ускоряет чтение без потери понимания."
            val spannable = SpannableString(text)
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), 0, name.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), text.indexOf("целыми смысловыми фрагментами"), text.indexOf("целыми смысловыми фрагментами") + "целыми смысловыми фрагментами".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), text.indexOf("сразу нескольких строк"), text.indexOf("сразу нескольких строк") + "сразу нескольких строк".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            return spannable
        }

    override fun startAnimation(
        textView: TextView,
        guideView: View,
        durationPerWord: Long,
        selectedTextIndex: Int,
        onAnimationEnd: () -> Unit
    ) {
        try {
            this.selectedTextIndex = selectedTextIndex
            fullText = TextResources.otherTexts["Чтение \"блоками\""]?.getOrNull(selectedTextIndex)?.text?.replace("\n", " ") ?: ""
            if (fullText.isEmpty()) {
                Log.e("BlockReading", "No text available for selectedTextIndex=$selectedTextIndex")
                textView.text = "Текст недоступен"
                onAnimationEnd()
                return
            }

            currentBlockIndex = 0
            lastScrollY = 0

            // Проверяем durationPerWord
            if (durationPerWord <= 0) {
                Log.e("BlockReading", "Invalid durationPerWord: $durationPerWord")
                textView.text = "Ошибка: некорректная скорость чтения"
                onAnimationEnd()
                return
            }

            // Преобразуем WPM в миллисекунды на слово
            val wordDurationMs = (60_000 / durationPerWord).coerceAtLeast(50L)
            Log.d("BlockReading", "Starting animation with durationPerWord=$durationPerWord WPM, wordDurationMs=$wordDurationMs ms, selectedTextIndex=$selectedTextIndex, textLength=${fullText.length}")

            scrollView = textView.parent as? ScrollView
            Log.d("BlockReading", "ScrollView initialized: $scrollView, parent=${textView.parent}, parentClass=${textView.parent?.javaClass?.simpleName}")
            if (scrollView == null) {
                Log.w("BlockReading", "TextView is not inside a ScrollView, scrolling will be disabled")
            } else {
                Log.d("BlockReading", "ScrollView height: ${scrollView?.height}, width: ${scrollView?.width}")
            }

            textView.gravity = android.view.Gravity.TOP
            textView.isSingleLine = false
            textView.maxLines = Int.MAX_VALUE
            textView.post {
                showNextTextPart(textView, guideView, wordDurationMs, onAnimationEnd)
            }
        } catch (e: Exception) {
            Log.e("BlockReading", "Error in startAnimation: ${e.message}", e)
            textView.text = "Ошибка анимации"
            onAnimationEnd()
        }
    }

    private fun showNextTextPart(
        textView: TextView,
        guideView: View,
        wordDurationMs: Long,
        onAnimationEnd: () -> Unit
    ) {
        currentPartText = fullText
        textView.text = currentPartText

        textView.post {
            val layout = textView.layout
            if (layout == null) {
                Log.e("BlockReading", "TextView layout is null in showNextTextPart")
                textView.text = "Ошибка отображения текста"
                onAnimationEnd()
                return@post
            }
            lineCount = layout.lineCount
            lines = (0 until lineCount).map { line ->
                layout.getLineStart(line)..layout.getLineEnd(line)
            }
            currentBlockIndex = 0

            Log.d("BlockReading", "Showing full text: '${currentPartText.take(50)}...', lineCount=$lineCount")
            Log.d("BlockReading", "Lines: ${lines.map { currentPartText.substring(it.first, it.last) }.joinToString(" | ")}")

            animateNextBlock(textView, guideView, wordDurationMs, onAnimationEnd)
        }
    }

    private fun animateNextBlock(
        textView: TextView,
        guideView: View,
        wordDurationMs: Long,
        onAnimationEnd: () -> Unit
    ) {
        if (currentBlockIndex * 2 >= lineCount) {
            guideView.visibility = View.INVISIBLE
            Log.d("BlockReading", "Text ended, stopping animation")
            animator?.cancel()
            textView.text = currentPartText
            onAnimationEnd()
            return
        }

        val (wordCountInBlock, firstLineWordCount, secondLineWordCount) = highlightBlock(textView)
        startBlockAnimation(textView, guideView, wordDurationMs, wordCountInBlock, firstLineWordCount, secondLineWordCount, onAnimationEnd)
    }

    private fun highlightBlock(textView: TextView): Triple<Int, Int, Int> {
        val spannable = SpannableString(currentPartText)
        val existingSpans = spannable.getSpans(0, spannable.length, BackgroundColorSpan::class.java)
        for (span in existingSpans) {
            spannable.removeSpan(span)
        }

        val firstLineIndex = currentBlockIndex * 2
        val secondLineIndex = min(firstLineIndex + 1, lineCount - 1)
        val startIndex = lines[firstLineIndex].first
        val endIndex = lines[secondLineIndex].last

        // Подсчитываем количество слов в блоке и в каждой строке
        val blockText = currentPartText.substring(startIndex, endIndex)
        val wordCountInBlock = blockText.split("\\s+".toRegex()).filter { it.isNotEmpty() }.size

        val firstLineText = currentPartText.substring(lines[firstLineIndex].first, lines[firstLineIndex].last)
        val firstLineWordCount = firstLineText.split("\\s+".toRegex()).filter { it.isNotEmpty() }.size

        val secondLineText = if (secondLineIndex > firstLineIndex) {
            currentPartText.substring(lines[secondLineIndex].first, lines[secondLineIndex].last)
        } else {
            ""
        }
        val secondLineWordCount = secondLineText.split("\\s+".toRegex()).filter { it.isNotEmpty() }.size

        if (startIndex < spannable.length && endIndex <= spannable.length) {
            spannable.setSpan(
                BackgroundColorSpan(Color.YELLOW),
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            Log.d("BlockReading", "Highlighting block: lines $firstLineIndex-$secondLineIndex, start=$startIndex, end=$endIndex, text='$blockText', wordCount=$wordCountInBlock, firstLineWords=$firstLineWordCount, secondLineWords=$secondLineWordCount")
        } else {
            Log.e("BlockReading", "Invalid block indices: start=$startIndex, end=$endIndex, spannable.length=${spannable.length}")
        }

        textView.text = spannable
        return Triple(wordCountInBlock, firstLineWordCount, secondLineWordCount)
    }

    private fun startBlockAnimation(
        textView: TextView,
        guideView: View,
        wordDurationMs: Long,
        wordCountInBlock: Int,
        firstLineWordCount: Int,
        secondLineWordCount: Int,
        onAnimationEnd: () -> Unit
    ) {
        guideView.visibility = View.INVISIBLE
        animator?.cancel()

        val layout = textView.layout
        if (layout == null) {
            Log.e("BlockReading", "TextView layout is null")
            textView.postDelayed({ animateNextBlock(textView, guideView, wordDurationMs, onAnimationEnd) }, 200)
            return
        }

        val firstLineIndex = currentBlockIndex * 2
        val secondLineIndex = min(firstLineIndex + 1, lineCount - 1)
        val startIndex = lines[firstLineIndex].first
        val endIndex = lines[secondLineIndex].last

        // Позиции для первой строки
        val firstLineStartX = layout.getLineLeft(firstLineIndex)
        val firstLineEndX = layout.getLineRight(firstLineIndex)
        val firstLineTop = layout.getLineTop(firstLineIndex).toFloat()
        val firstLineBottom = layout.getLineBottom(firstLineIndex).toFloat()
        val firstLineY = (firstLineTop + firstLineBottom) / 2

        // Позиции для второй строки
        val secondLineStartX = layout.getLineLeft(secondLineIndex)
        val secondLineEndX = layout.getLineRight(secondLineIndex)
        val secondLineTop = layout.getLineTop(secondLineIndex).toFloat()
        val secondLineBottom = layout.getLineBottom(secondLineIndex).toFloat()
        val secondLineY = (secondLineTop + secondLineBottom) / 2

        // Рассчитываем длительность анимации блока
        val blockDurationMs = (wordCountInBlock * wordDurationMs).coerceAtLeast(50L)

        // Распределяем длительность между строками пропорционально количеству слов
        val totalWords = firstLineWordCount + secondLineWordCount
        val firstLineDuration = if (totalWords > 0) {
            (blockDurationMs * firstLineWordCount / totalWords.toFloat()).toLong().coerceAtLeast(50L)
        } else {
            blockDurationMs / 2
        }
        val secondLineDuration = (blockDurationMs - firstLineDuration).coerceAtLeast(50L)

        // Прокрутка ScrollView
        scrollView?.let { sv ->
            sv.post {
                val scrollViewHeight = sv.height
                val currentScrollY = sv.scrollY
                val lineTopPosition = layout.getLineTop(firstLineIndex)
                val lineBottomPosition = layout.getLineBottom(secondLineIndex)

                // Определяем видимую область (верхняя треть экрана)
                val visibleTop = currentScrollY
                val visibleBottom = currentScrollY + scrollViewHeight * 2 / 3

                // Прокручиваем, если блок не полностью виден
                if (lineTopPosition < visibleTop || lineBottomPosition > visibleBottom) {
                    // Цель: поставить верх блока в верхнюю треть экрана
                    val targetScrollY = (lineTopPosition - scrollViewHeight / 3).coerceAtLeast(0).toInt()
                    if (targetScrollY != lastScrollY) {
                        Log.d("BlockReading", "Attempting scroll for block $currentBlockIndex, lines $firstLineIndex-$secondLineIndex")
                        Log.d("BlockReading", "Scroll parameters: block=$currentBlockIndex, lineTop=$lineTopPosition, lineBottom=$lineBottomPosition, scrollViewHeight=$scrollViewHeight, currentScrollY=$currentScrollY, targetScrollY=$targetScrollY")
                        // Плавная прокрутка
                        ValueAnimator.ofInt(currentScrollY, targetScrollY).apply {
                            duration = blockDurationMs / 2 // Прокрутка быстрее анимации блока
                            addUpdateListener { animation ->
                                val value = animation.animatedValue as Int
                                sv.scrollTo(0, value)
                            }
                            addListener(
                                onEnd = {
                                    lastScrollY = targetScrollY
                                    Log.d("BlockReading", "Scrolled to block $currentBlockIndex, targetScrollY=$targetScrollY, currentScrollY=${sv.scrollY}")
                                }
                            )
                            start()
                        }
                    } else {
                        Log.d("BlockReading", "No scroll needed, already at target: block=$currentBlockIndex, targetScrollY=$targetScrollY")
                    }
                } else {
                    Log.d("BlockReading", "No scroll needed, block $currentBlockIndex is visible, lineTop=$lineTopPosition, lineBottom=$lineBottomPosition, visibleTop=$visibleTop, visibleBottom=$visibleBottom")
                }

                sv.postDelayed({
                    Log.d("BlockReading", "After scroll check, currentScrollY=${sv.scrollY}, textViewHeight=${textView.height}, scrollViewHeight=$scrollViewHeight")
                }, 100)
            }
        }

        Log.d("BlockReading", "Animating block: $currentBlockIndex, firstLine: startX=$firstLineStartX, endX=$firstLineEndX, y=$firstLineY, duration=$firstLineDuration ms; secondLine: startX=$secondLineStartX, endX=$secondLineEndX, y=$secondLineY, duration=$secondLineDuration ms; wordCount=$wordCountInBlock, totalDuration=$blockDurationMs ms")

        // Анимация первой строки
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = firstLineDuration
            addUpdateListener { animation ->
                val fraction = animation.animatedValue as Float
                val currentX = firstLineStartX + (firstLineEndX - firstLineStartX) * fraction
                guideView.translationX = currentX - (guideView.width / 2) + textView.left
                guideView.translationY = firstLineY + textView.top.toFloat() - (scrollView?.scrollY?.toFloat() ?: 0f)
            }
            addListener(
                onEnd = {
                    // Анимация второй строки
                    animator = ValueAnimator.ofFloat(0f, 1f).apply {
                        duration = secondLineDuration
                        addUpdateListener { animation ->
                            val fraction = animation.animatedValue as Float
                            val currentX = secondLineStartX + (secondLineEndX - secondLineStartX) * fraction
                            guideView.translationX = currentX - (guideView.width / 2) + textView.left
                            guideView.translationY = secondLineY + textView.top.toFloat() - (scrollView?.scrollY?.toFloat() ?: 0f)
                        }
                        addListener(
                            onEnd = {
                                currentBlockIndex++
                                Log.d("BlockReading", "Block animation ended, currentBlockIndex=$currentBlockIndex")
                                animateNextBlock(textView, guideView, wordDurationMs, onAnimationEnd)
                            }
                        )
                        start()
                    }
                }
            )
            start()
        }
    }
}