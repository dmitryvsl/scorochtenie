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
class SentenceReverseTechnique : ReadingTechnique("SentenceReverseTechnique", "Предложения наоборот") {
    private var currentSentenceIndex = 0
    private var currentWordIndexInSentence = 0
    private var selectedTextIndex = 0
    private var fullText: String = ""
    private var currentPosition = 0
    private var animator: ValueAnimator? = null
    private var sentences: List<List<String>> = emptyList()
    private var sentenceStartIndices: List<Int> = emptyList()
    private var scrollView: ScrollView? = null
    private var lastScrollY: Int = 0
    private val handler = Handler(Looper.getMainLooper())
    private var isAnimationActive = false

    override val description: SpannableString
        get() {
            val text = "Предложения наоборот — это техника скорочтения, направленная на формирование навыка правильного чтения и профилактику «зеркального» чтения. Текст читается, начиная с последнего слова каждого предложения.\n" +
                    "Для применения техники начинайте с последнего слова предложения и двигайтесь к первому.\n" +
                    "Сосредоточьтесь на правильном порядке чтения слов, чтобы улучшить внимание и навыки чтения."
            val spannable = SpannableString(text)
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), 0, name.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), text.indexOf("начинайте с последнего слова"), text.indexOf("начинайте с последнего слова") + "начинайте с последнего слова".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), text.indexOf("правильном порядке чтения"), text.indexOf("правильном порядке чтения") + "правильном порядке чтения".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
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
        fullText = reverseSentences(TextResources.otherTexts["Предложения наоборот"]?.getOrNull(selectedTextIndex)?.text?.replace("\n", " ") ?: "")
        currentPosition = 0
        currentSentenceIndex = 0
        currentWordIndexInSentence = 0
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
                showText(textView, guideView, wordDurationMs, onAnimationEnd)
            }
        }
    }

    private fun showText(
        textView: TextView,
        guideView: View,
        wordDurationMs: Long,
        onAnimationEnd: () -> Unit
    ) {
        if (!isAnimationActive) return

        if (currentPosition >= fullText.length) {
            guideView.visibility = View.INVISIBLE
            animator?.cancel()
            textView.text = fullText
            if (isAnimationActive) onAnimationEnd()
            return
        }

        textView.text = fullText
        sentences = parseSentences(fullText)
        sentenceStartIndices = calculateSentenceStartIndices(fullText, sentences)

        val currentSentence = sentences.getOrNull(currentSentenceIndex)
        currentWordIndexInSentence = if (currentSentence.isNullOrEmpty()) -1 else currentSentence.size - 1

        handler.post {
            if (isAnimationActive) {
                animateNextWord(textView, guideView, wordDurationMs, onAnimationEnd)
            }
        }
    }

    private fun parseSentences(text: String): List<List<String>> {
        val sentences = mutableListOf<List<String>>()
        val wordRegex = Regex("""\b\w+\b""")
        val sentenceRegex = Regex("([^.!?()]+[.!?])")

        sentenceRegex.findAll(text).forEach { matchResult ->
            val sentenceText = matchResult.value.trim()
            val words = wordRegex.findAll(sentenceText)
                .map { it.value }
                .filter { it.isNotEmpty() }
                .toList()
            if (words.isNotEmpty()) {
                sentences.add(words)
            }
        }

        if (sentences.isEmpty()) {
            val words = wordRegex.findAll(text)
                .map { it.value }
                .filter { it.isNotEmpty() }
                .toList()
            if (words.isNotEmpty()) {
                sentences.add(words)
            }
        }

        return sentences
    }

    private fun calculateSentenceStartIndices(text: String, sentences: List<List<String>>): List<Int> {
        val indices = mutableListOf<Int>()
        var currentIndex = 0
        sentences.forEach { sentence ->
            indices.add(currentIndex)
            sentence.forEach { word ->
                var wordIndex = text.indexOf(word, currentIndex)
                while (wordIndex != -1) {
                    val isWordStart = wordIndex == 0 || !text[wordIndex - 1].isLetterOrDigit()
                    val wordEnd = wordIndex + word.length
                    val isWordEnd = wordEnd == text.length || !text[wordEnd].isLetterOrDigit()
                    if (isWordStart && isWordEnd) {
                        break
                    }
                    wordIndex = text.indexOf(word, wordIndex + 1)
                }
                if (wordIndex == -1) {
                    return@forEach
                }
                currentIndex = wordIndex + word.length
                while (currentIndex < text.length && text[currentIndex] == ' ') {
                    currentIndex++
                }
            }
            while (currentIndex < text.length && !text[currentIndex].isLetterOrDigit()) {
                currentIndex++
            }
        }
        return indices
    }

    private fun reverseSentences(text: String): String {
        val sentenceRegex = Regex("([^.!?]+)([.!?])")
        return sentenceRegex.findAll(text).joinToString(" ") { matchResult ->
            val body = matchResult.groupValues[1].trim()
            val endPunct = matchResult.groupValues[2]
            val tokenRegex = Regex("""\w+|[^\s\w]""")
            val tokens = tokenRegex.findAll(body).map { it.value }.toList()

            val wordChunks = mutableListOf<WordChunk>()
            var i = 0
            while (i < tokens.size) {
                val tok = tokens[i]
                if (tok.any { it.isLetterOrDigit() }) {
                    val puncts = mutableListOf<String>()
                    var j = i + 1
                    while (j < tokens.size && !tokens[j].any { it.isLetterOrDigit() }) {
                        puncts.add(tokens[j])
                        j++
                    }
                    wordChunks.add(WordChunk(tok, puncts))
                    i = j
                } else {
                    if (wordChunks.isNotEmpty()) {
                        wordChunks.last().punctuation.add(tok)
                    }
                    i++
                }
            }

            val sb = StringBuilder()
            for ((word, puncts) in wordChunks.asReversed()) {
                for (p in puncts) {
                    sb.append(p)
                }
                if (sb.isNotEmpty() && sb.last() != ' ' && sb.last() !in listOf('-', '—', '–', ',', '.', '!', '?')) {
                    sb.append(' ')
                }
                sb.append(word)
                sb.append(' ')
            }

            var sent = sb.toString().trim()
            val wordPattern = Regex("""\b([a-zA-Zа-яА-ЯёЁ]+)\b""")
            val lastWordMatch = wordPattern.findAll(sent).lastOrNull()
            if (lastWordMatch != null) {
                val lastWord = lastWordMatch.value
                val range = lastWordMatch.range
                val correctedLastWord = lastWord.replaceFirstChar { it.lowercaseChar() }
                sent = sent.substring(0, range.start) + correctedLastWord + sent.substring(range.endInclusive + 1)
            }

            val finalSent = sent.replaceFirstChar { it.uppercaseChar() }
            "$finalSent$endPunct"
        }
    }

    private fun animateNextWord(
        textView: TextView,
        guideView: View,
        wordDurationMs: Long,
        onAnimationEnd: () -> Unit
    ) {
        if (!isAnimationActive) return

        if (currentSentenceIndex >= sentences.size) {
            guideView.visibility = View.INVISIBLE
            animator?.cancel()
            textView.text = fullText
            if (isAnimationActive) onAnimationEnd()
            return
        }

        val currentSentence = sentences.getOrNull(currentSentenceIndex)
        if (currentSentence == null || currentSentence.isEmpty() || currentWordIndexInSentence < 0) {
            currentSentenceIndex++
            val nextSentence = sentences.getOrNull(currentSentenceIndex)
            currentWordIndexInSentence = if (nextSentence.isNullOrEmpty()) -1 else nextSentence.size - 1
            handler.post {
                if (isAnimationActive) animateNextWord(textView, guideView, wordDurationMs, onAnimationEnd)
            }
            return
        }

        handler.post {
            if (isAnimationActive) {
                highlightWord(textView)
                startWordAnimation(textView, guideView, wordDurationMs, onAnimationEnd)
            }
        }
    }

    private fun highlightWord(textView: TextView) {
        if (!isAnimationActive) return

        val spannable = SpannableString(fullText)
        val existingSpans = spannable.getSpans(0, spannable.length, BackgroundColorSpan::class.java)
        for (span in existingSpans) {
            spannable.removeSpan(span)
        }

        val currentSentence = sentences.getOrNull(currentSentenceIndex)
        if (currentSentence == null || currentSentence.isEmpty() || currentWordIndexInSentence < 0 || currentWordIndexInSentence >= currentSentence.size) {
            return
        }

        val word = currentSentence[currentWordIndexInSentence]
        val sentenceStartIndex = sentenceStartIndices.getOrNull(currentSentenceIndex) ?: 0
        val sentenceEndIndex = if (currentSentenceIndex + 1 < sentenceStartIndices.size) {
            sentenceStartIndices[currentSentenceIndex + 1]
        } else {
            fullText.length
        }
        val wordStartIndex = findWordStartIndex(word, sentenceStartIndex, sentenceEndIndex, currentSentence, currentWordIndexInSentence)

        if (wordStartIndex >= 0 && wordStartIndex < fullText.length) {
            val endIndex = wordStartIndex + word.length
            if (endIndex <= fullText.length) {
                spannable.setSpan(
                    BackgroundColorSpan(Color.YELLOW),
                    wordStartIndex,
                    endIndex,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        textView.text = spannable
    }

    private fun findWordStartIndex(word: String, startIndex: Int, endIndex: Int, sentence: List<String>, wordPosition: Int): Int {
        if (startIndex < 0 || startIndex >= fullText.length || endIndex > fullText.length || startIndex >= endIndex) {
            return -1
        }

        var currentIndex = startIndex
        var currentWordIdx = 0
        while (currentIndex < endIndex && currentWordIdx <= wordPosition) {
            while (currentIndex < endIndex && !fullText[currentIndex].isLetterOrDigit()) {
                currentIndex++
            }
            if (currentIndex >= endIndex) break

            val currentWord = sentence[currentWordIdx]
            val isWordStart = currentIndex == 0 || !fullText[currentIndex - 1].isLetterOrDigit()
            val wordEnd = currentIndex + currentWord.length
            val isWordEnd = wordEnd >= fullText.length || !fullText[wordEnd].isLetterOrDigit()
            if (isWordStart && isWordEnd && fullText.substring(currentIndex, wordEnd) == currentWord) {
                if (currentWordIdx == wordPosition) {
                    return currentIndex
                }
                currentIndex = wordEnd
                currentWordIdx++
            } else {
                while (currentIndex < endIndex && fullText[currentIndex].isLetterOrDigit()) {
                    currentIndex++
                }
            }
        }

        return -1
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

        val currentSentence = sentences.getOrNull(currentSentenceIndex)
        if (currentSentence == null || currentSentence.isEmpty() || currentWordIndexInSentence < 0 || currentWordIndexInSentence >= currentSentence.size) {
            currentWordIndexInSentence--
            handler.postDelayed({
                if (isAnimationActive) animateNextWord(textView, guideView, wordDurationMs, onAnimationEnd)
            }, 200)
            return
        }

        val word = currentSentence[currentWordIndexInSentence]
        val sentenceStartIndex = sentenceStartIndices.getOrNull(currentSentenceIndex) ?: 0
        val sentenceEndIndex = if (currentSentenceIndex + 1 < sentenceStartIndices.size) {
            sentenceStartIndices[currentSentenceIndex + 1]
        } else {
            fullText.length
        }
        val wordStartIndex = findWordStartIndex(word, sentenceStartIndex, sentenceEndIndex, currentSentence, currentWordIndexInSentence)

        if (wordStartIndex < 0 || wordStartIndex >= fullText.length) {
            handler.postDelayed({
                if (isAnimationActive) animateNextWord(textView, guideView, wordDurationMs, onAnimationEnd)
            }, 200)
            return
        }

        val wordEndIndex = wordStartIndex + word.length
        if (wordEndIndex > fullText.length) {
            handler.postDelayed({
                if (isAnimationActive) animateNextWord(textView, guideView, wordDurationMs, onAnimationEnd)
            }, 200)
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
                        currentWordIndexInSentence--
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

    data class WordChunk(val word: String, val punctuation: MutableList<String>)
}