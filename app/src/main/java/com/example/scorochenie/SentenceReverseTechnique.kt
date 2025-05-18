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

class SentenceReverseTechnique : ReadingTechnique("Предложения наоборот") {
    private var currentSentenceIndex = 0
    private var currentWordIndexInSentence = 0
    private var selectedTextIndex = 0
    private var fullText: String = ""
    private var currentPosition = 0
    private var animator: ValueAnimator? = null
    private var sentences: List<List<String>> = emptyList()
    private var sentenceStartIndices: List<Int> = emptyList()
    private var scrollView: ScrollView? = null
    private var lastScrollY: Int = 0 // Последняя позиция прокрутки

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
        onAnimationEnd: () -> Unit
    ) {
        selectedTextIndex = Random.nextInt(TextResources.sampleTexts.size)
        fullText = reverseSentences(TextResources.sampleTexts[selectedTextIndex]).replace("\n", " ")
        currentPosition = 0
        currentSentenceIndex = 0
        currentWordIndexInSentence = 0
        lastScrollY = 0

        // Проверяем, что TextView находится внутри ScrollView
        scrollView = textView.parent as? ScrollView
        Log.d("SentenceReverse", "ScrollView initialized: $scrollView, parent=${textView.parent}, parentClass=${textView.parent?.javaClass?.simpleName}")
        if (scrollView == null) {
            Log.e("SentenceReverse", "TextView is not inside a ScrollView, scrolling will not work")
        } else {
            Log.d("SentenceReverse", "ScrollView height: ${scrollView?.height}, width: ${scrollView?.width}")
        }

        textView.gravity = android.view.Gravity.TOP
        textView.isSingleLine = false
        textView.maxLines = Int.MAX_VALUE
        textView.post {
            Log.d("SentenceReverse", "Full text length: ${fullText.length}")
            Log.d("SentenceReverse", "TextView height: ${textView.height}, width: ${textView.width}, lineCount: ${textView.lineCount}")
            showText(textView, guideView, onAnimationEnd)
        }
    }

    private fun showText(
        textView: TextView,
        guideView: View,
        onAnimationEnd: () -> Unit
    ) {
        if (currentPosition >= fullText.length) {
            guideView.visibility = View.INVISIBLE
            Log.d("SentenceReverse", "Text ended, stopping animation")
            animator?.cancel()
            textView.text = fullText
            onAnimationEnd()
            return
        }

        textView.text = fullText
        sentences = parseSentences(fullText)
        sentenceStartIndices = calculateSentenceStartIndices(fullText, sentences)

        val currentSentence = sentences.getOrNull(currentSentenceIndex)
        currentWordIndexInSentence = if (currentSentence.isNullOrEmpty()) -1 else currentSentence.size - 1

        Log.d("SentenceReverse", "Showing text: '$fullText'")
        Log.d("SentenceReverse", "Sentences: ${sentences.map { it.joinToString(" ") }}")
        Log.d("SentenceReverse", "Sentence start indices: $sentenceStartIndices")
        Log.d("SentenceReverse", "Initial currentSentenceIndex=$currentSentenceIndex, currentWordIndexInSentence=$currentWordIndexInSentence, currentSentence=${currentSentence?.joinToString(" ")}")
        if (currentSentence != null && currentWordIndexInSentence >= 0) {
            Log.d("SentenceReverse", "Starting with word: '${currentSentence[currentWordIndexInSentence]}' at position $currentWordIndexInSentence")
        }

        textView.post {
            animateNextWord(textView, guideView, onAnimationEnd)
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
                Log.d("SentenceReverse", "Parsed sentence: ${words.joinToString(" ")} (word count: ${words.size})")
            }
        }

        if (sentences.isEmpty()) {
            Log.w("SentenceReverse", "No sentences parsed from text: $text")
            val words = wordRegex.findAll(text)
                .map { it.value }
                .filter { it.isNotEmpty() }
                .toList()
            if (words.isNotEmpty()) {
                sentences.add(words)
                Log.d("SentenceReverse", "Fallback parsed sentence: ${words.joinToString(" ")} (word count: ${words.size})")
            }
        }

        Log.d("SentenceReverse", "Parsed sentences: ${sentences.map { it.joinToString(" ") }}")
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
                    Log.e("SentenceReverse", "Word '$word' not found from index $currentIndex in text: $text")
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
        Log.d("SentenceReverse", "Sentence start indices: $indices")
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
        onAnimationEnd: () -> Unit
    ) {
        Log.d("SentenceReverse", "animateNextWord: currentSentenceIndex=$currentSentenceIndex, currentWordIndexInSentence=$currentWordIndexInSentence")

        if (currentSentenceIndex >= sentences.size) {
            Log.d("SentenceReverse", "No more sentences to display, ending animation")
            guideView.visibility = View.INVISIBLE
            animator?.cancel()
            textView.text = fullText
            onAnimationEnd()
            return
        }

        val currentSentence = sentences.getOrNull(currentSentenceIndex)
        if (currentSentence == null || currentSentence.isEmpty() || currentWordIndexInSentence < 0) {
            currentSentenceIndex++
            val nextSentence = sentences.getOrNull(currentSentenceIndex)
            currentWordIndexInSentence = if (nextSentence.isNullOrEmpty()) -1 else nextSentence.size - 1
            Log.d("SentenceReverse", "Moving to next sentence: currentSentenceIndex=$currentSentenceIndex, currentWordIndexInSentence=$currentWordIndexInSentence, nextSentence=${nextSentence?.joinToString(" ")}")
            if (nextSentence != null && currentWordIndexInSentence >= 0) {
                Log.d("SentenceReverse", "Starting with word: '${nextSentence[currentWordIndexInSentence]}' at position $currentWordIndexInSentence")
            }
            textView.post { animateNextWord(textView, guideView, onAnimationEnd) }
            return
        }

        Log.d("SentenceReverse", "Starting sentence: ${currentSentence.joinToString(" ")} (word count: ${currentSentence.size})")
        Log.d("SentenceReverse", "Current word: '${currentSentence[currentWordIndexInSentence]}' at position $currentWordIndexInSentence")

        textView.post {
            highlightWord(textView)
            startWordAnimation(textView, guideView, onAnimationEnd)
        }
    }

    private fun highlightWord(textView: TextView) {
        val spannable = SpannableString(fullText)
        val existingSpans = spannable.getSpans(0, spannable.length, BackgroundColorSpan::class.java)
        for (span in existingSpans) {
            spannable.removeSpan(span)
        }

        val currentSentence = sentences.getOrNull(currentSentenceIndex)
        if (currentSentence == null || currentSentence.isEmpty() || currentWordIndexInSentence < 0 || currentWordIndexInSentence >= currentSentence.size) {
            Log.e("SentenceReverse", "Invalid sentence or word index: currentSentenceIndex=$currentSentenceIndex, currentWordIndexInSentence=$currentWordIndexInSentence, sentence=${currentSentence?.joinToString(" ")}")
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
                Log.d("SentenceReverse", "Highlighting word: '$word' at position $currentWordIndexInSentence, start=$wordStartIndex, end=$endIndex")
            } else {
                Log.e("SentenceReverse", "Invalid end index: $endIndex for word: '$word'")
            }
        } else {
            Log.e("SentenceReverse", "Invalid word start index: $wordStartIndex for word: '$word'")
        }

        textView.text = spannable
    }

    private fun findWordStartIndex(word: String, startIndex: Int, endIndex: Int, sentence: List<String>, wordPosition: Int): Int {
        if (startIndex < 0 || startIndex >= fullText.length || endIndex > fullText.length || startIndex >= endIndex) {
            Log.e("SentenceReverse", "Invalid indices: startIndex=$startIndex, endIndex=$endIndex")
            return -1
        }

        val sentenceText = fullText.substring(startIndex, endIndex)
        Log.d("SentenceReverse", "Current sentence text: '$sentenceText'")
        Log.d("SentenceReverse", "Current sentence: ${sentence.joinToString(" ")}")

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

        Log.e("SentenceReverse", "Word '$word' at position $wordPosition not found in sentence text: '$sentenceText'")
        return -1
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
            Log.e("SentenceReverse", "TextView layout is null")
            textView.postDelayed({ animateNextWord(textView, guideView, onAnimationEnd) }, 200)
            return
        }

        val currentSentence = sentences.getOrNull(currentSentenceIndex)
        if (currentSentence == null || currentSentence.isEmpty() || currentWordIndexInSentence < 0 || currentWordIndexInSentence >= currentSentence.size) {
            Log.e("SentenceReverse", "Invalid sentence or word index: currentSentenceIndex=$currentSentenceIndex, currentWordIndexInSentence=$currentWordIndexInSentence, sentence=${currentSentence?.joinToString(" ")}")
            currentWordIndexInSentence--
            textView.postDelayed({ animateNextWord(textView, guideView, onAnimationEnd) }, 200)
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
            Log.e("SentenceReverse", "Invalid wordStartIndex: $wordStartIndex for word: '$word'")
            textView.postDelayed({ animateNextWord(textView, guideView, onAnimationEnd) }, 200)
            return
        }

        val wordEndIndex = wordStartIndex + word.length
        if (wordEndIndex > fullText.length) {
            Log.e("SentenceReverse", "Invalid wordEndIndex: $wordEndIndex for word: '$word'")
            textView.postDelayed({ animateNextWord(textView, guideView, onAnimationEnd) }, 200)
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
                        Log.d("SentenceReverse", "Attempting scroll for line $startLine, word='$word'")
                        Log.d("SentenceReverse", "Scroll parameters: line=$startLine, word='$word', lineTop=$lineTopPosition, lineBottom=$lineBottomPosition, scrollViewHeight=$scrollViewHeight, currentScrollY=$currentScrollY, targetScrollY=$targetScrollY")
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
                                    Log.d("SentenceReverse", "Scrolled to line $startLine, targetScrollY=$targetScrollY, currentScrollY=${sv.scrollY}")
                                }
                            )
                            start()
                        }
                    } else {
                        Log.d("SentenceReverse", "No scroll needed, already at target: line=$startLine, word='$word', targetScrollY=$targetScrollY")
                    }
                } else {
                    Log.d("SentenceReverse", "No scroll needed, line $startLine is visible, lineTop=$lineTopPosition, lineBottom=$lineBottomPosition, visibleTop=$visibleTop, visibleBottom=$visibleBottom")
                }

                sv.postDelayed({
                    Log.d("SentenceReverse", "After scroll check, currentScrollY=${sv.scrollY}, textViewHeight=${textView.height}, scrollViewHeight=$scrollViewHeight")
                }, 100)
            }
        } ?: Log.e("SentenceReverse", "ScrollView is null, cannot scroll to line $startLine for word '$word'")

        Log.d("SentenceReverse", "Animating word: '$word' at position $currentWordIndexInSentence, startX=$startX, endX=$endX, lineY=$lineY, startLine=$startLine, endLine=$endLine")

        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1L // Исправлено: более заметная анимация слова
            addUpdateListener { animation ->
                val fraction = animation.animatedValue as Float
                val currentX = startX + (endX - startX) * fraction
                // Корректируем позицию guideView с учётом прокрутки
                guideView.translationX = currentX - (guideView.width / 2) + textView.left
                guideView.translationY = lineY + textView.top.toFloat() - (scrollView?.scrollY?.toFloat() ?: 0f)
                Log.d("SentenceReverse", "guideView position: translationX=${guideView.translationX}, translationY=${guideView.translationY}, scrollY=${scrollView?.scrollY}")
            }
            addListener(
                onEnd = {
                    currentWordIndexInSentence--
                    Log.d("SentenceReverse", "Word animation ended, currentSentenceIndex=$currentSentenceIndex, currentWordIndexInSentence=$currentWordIndexInSentence")
                    textView.postDelayed({ animateNextWord(textView, guideView, onAnimationEnd) }, 200)
                }
            )
            start()
        }
    }

    data class WordChunk(val word: String, val punctuation: MutableList<String>)
}