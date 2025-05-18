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
            val text = "Чтение \"блоками\" — это техника скорочтения, при которой текст воспринимается целыми смысловыми блоками, а не отдельными словами. Метод ускоряет обработку информации за счет группировки слов в логические единицы.\n" +
                    "Чтобы применять эту технику, захватывайте взглядом группы слов одновременно, объединяя их в смысловые фрагменты.\n" +
                    "Сосредоточьтесь на целостных идеях, чтобы быстрее уловить суть текста без лишнего чтения."
            val spannable = SpannableString(text)
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), 0, name.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), text.indexOf("захватывайте взглядом группы слов"), text.indexOf("захватывайте взглядом группы слов") + "захватывайте взглядом группы слов".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), text.indexOf("смысловые фрагменты"), text.indexOf("смысловые фрагменты") + "смысловые фрагменты".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), text.indexOf("целостных идеях"), text.indexOf("целостных идеях") + "целостных идеях".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            return spannable
        }

    override fun startAnimation(
        textView: TextView,
        guideView: View,
        onAnimationEnd: () -> Unit
    ) {
        selectedTextIndex = Random.nextInt(TextResources.sampleTexts.size)
        fullText = TextResources.sampleTexts[selectedTextIndex].replace("\n", " ")
        currentBlockIndex = 0
        lastScrollY = 0

        scrollView = textView.parent as? ScrollView
        Log.d("BlockReading", "ScrollView initialized: $scrollView, parent=${textView.parent}, parentClass=${textView.parent?.javaClass?.simpleName}")
        if (scrollView == null) {
            Log.e("BlockReading", "TextView is not inside a ScrollView, scrolling will not work")
        } else {
            Log.d("BlockReading", "ScrollView height: ${scrollView?.height}, width: ${scrollView?.width}")
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
        textView.text = currentPartText

        textView.post {
            val layout = textView.layout ?: return@post
            lineCount = layout.lineCount
            lines = (0 until lineCount).map { line ->
                layout.getLineStart(line)..layout.getLineEnd(line)
            }
            currentBlockIndex = 0

            Log.d("BlockReading", "Showing full text: '$currentPartText', lineCount=$lineCount")
            Log.d("BlockReading", "Lines: ${lines.map { currentPartText.substring(it.first, it.last) }.joinToString(" | ")}")

            animateNextBlock(textView, guideView, onAnimationEnd)
        }
    }

    private fun animateNextBlock(
        textView: TextView,
        guideView: View,
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

        highlightBlock(textView)
        startBlockAnimation(textView, guideView, onAnimationEnd)
    }

    private fun highlightBlock(textView: TextView) {
        val spannable = SpannableString(currentPartText)
        val existingSpans = spannable.getSpans(0, spannable.length, BackgroundColorSpan::class.java)
        for (span in existingSpans) {
            spannable.removeSpan(span)
        }

        val firstLineIndex = currentBlockIndex * 2
        val secondLineIndex = minOf(firstLineIndex + 1, lineCount - 1)
        val startIndex = lines[firstLineIndex].first
        val endIndex = lines[secondLineIndex].last

        if (startIndex < spannable.length && endIndex <= spannable.length) {
            spannable.setSpan(
                BackgroundColorSpan(Color.YELLOW),
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            Log.d("BlockReading", "Highlighting block: lines $firstLineIndex-$secondLineIndex, start=$startIndex, end=$endIndex, text='${currentPartText.substring(startIndex, endIndex)}'")
        }

        textView.text = spannable
    }

    private fun startBlockAnimation(
        textView: TextView,
        guideView: View,
        onAnimationEnd: () -> Unit
    ) {
        guideView.visibility = View.VISIBLE
        animator?.cancel()

        val layout = textView.layout
        if (layout == null) {
            Log.e("BlockReading", "TextView layout is null")
            textView.postDelayed({ animateNextBlock(textView, guideView, onAnimationEnd) }, 200)
            return
        }

        val firstLineIndex = currentBlockIndex * 2
        val secondLineIndex = minOf(firstLineIndex + 1, lineCount - 1)
        val startIndex = lines[firstLineIndex].first
        val endIndex = lines[secondLineIndex].last

        val startLine = firstLineIndex
        val endLine = secondLineIndex
        val startX = layout.getLineLeft(startLine)
        val endX = layout.getLineRight(endLine)
        val lineTop = layout.getLineTop(startLine).toFloat()
        val blockHeight = layout.getLineBottom(endLine) - layout.getLineTop(startLine)
        val lineY = lineTop + (blockHeight / 2)

        // Прокрутка ScrollView
        scrollView?.let { sv ->
            sv.post {
                val scrollViewHeight = sv.height
                val currentScrollY = sv.scrollY
                val lineTopPosition = layout.getLineTop(startLine)
                val lineBottomPosition = layout.getLineBottom(endLine)

                // Определяем видимую область (верхняя треть экрана)
                val visibleTop = currentScrollY
                val visibleBottom = currentScrollY + scrollViewHeight * 2 / 3

                // Прокручиваем, если блок не полностью виден
                if (lineTopPosition < visibleTop || lineBottomPosition > visibleBottom) {
                    // Цель: поставить верх блока в верхнюю треть экрана
                    val targetScrollY = (lineTopPosition - scrollViewHeight / 3).coerceAtLeast(0).toInt()
                    if (targetScrollY != lastScrollY) {
                        Log.d("BlockReading", "Attempting scroll for block $currentBlockIndex, lines $startLine-$endLine")
                        Log.d("BlockReading", "Scroll parameters: block=$currentBlockIndex, lineTop=$lineTopPosition, lineBottom=$lineBottomPosition, scrollViewHeight=$scrollViewHeight, currentScrollY=$currentScrollY, targetScrollY=$targetScrollY")
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
        } ?: Log.e("BlockReading", "ScrollView is null, cannot scroll to block $currentBlockIndex")

        Log.d("BlockReading", "Animating block: $currentBlockIndex, startX=$startX, endX=$endX, lineY=$lineY, startLine=$startLine, endLine=$endLine")

        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 600L
            addUpdateListener { animation ->
                val fraction = animation.animatedValue as Float
                val currentX = startX + (endX - startX) * fraction
                guideView.translationX = currentX - (guideView.width / 2) + textView.left
                guideView.translationY = lineY + textView.top.toFloat() - (scrollView?.scrollY?.toFloat() ?: 0f)
            }
            addListener(
                onEnd = {
                    currentBlockIndex++
                    Log.d("BlockReading", "Block animation ended, currentBlockIndex=$currentBlockIndex")
                    animateNextBlock(textView, guideView, onAnimationEnd)
                }
            )
            start()
        }
    }
}