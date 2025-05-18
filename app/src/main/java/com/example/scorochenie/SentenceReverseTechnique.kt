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
import kotlin.random.Random

class SentenceReverseTechnique : ReadingTechnique("Предложения наоборот") {
    private var currentSentenceIndex = 0
    private var currentWordIndexInSentence = 0
    private var selectedTextIndex = 0
    private var fullText: String = ""
    private var currentPosition = 0
    private var breakWordIndex = 0
    private var currentPartText: String = ""
    private var animator: ValueAnimator? = null
    private var sentences: List<List<String>> = emptyList()
    private var sentenceStartIndices: List<Int> = emptyList()
    private var previousParts: MutableList<String> = mutableListOf()
    private var currentSentenceStartIndexInFullText: Int = -1
    private var isAnimatingNextPage: Boolean = false
    private var pagePositions: List<Int> = emptyList()
    private var returnToPageIndex: Int = -1
    private var returnToSentenceIndex: Int = -1
    private var isReturningToFirstPage: Boolean = false
    private var currentFullSentenceWords: List<String> = emptyList()
    private var firstPageWordsRead: List<String> = emptyList()

    private val reverseBreakWords = listOf(
        listOf("наземные первые. Лишайники"),
        listOf("приёмник. Звука", "вокруг километров тысячи"),
        listOf("волны когда.")
    )

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
        val originalText = TextResources.sampleTexts[selectedTextIndex]
        fullText = reverseSentences(originalText).replace("\n", " ")
        currentPosition = 0
        breakWordIndex = 0
        currentSentenceIndex = 0
        currentWordIndexInSentence = 0
        previousParts.clear()
        isAnimatingNextPage = false
        isReturningToFirstPage = false
        returnToPageIndex = -1
        returnToSentenceIndex = -1
        currentFullSentenceWords = emptyList()
        firstPageWordsRead = emptyList()

        pagePositions = calculatePagePositions(fullText, reverseBreakWords[selectedTextIndex])
        Log.d("SentenceReverse", "Page positions: $pagePositions")

        textView.gravity = android.view.Gravity.TOP
        textView.isSingleLine = false
        textView.maxLines = Int.MAX_VALUE
        textView.post {
            Log.d("SentenceReverse", "Full text: $fullText")
            Log.d("SentenceReverse", "Starting from page $breakWordIndex, position $currentPosition")
            showNextTextPart(textView, guideView, onAnimationEnd)
        }
    }

    private fun calculatePagePositions(text: String, breakWords: List<String>): List<Int> {
        val positions = mutableListOf<Int>()
        positions.add(0)
        var currentPos = 0
        breakWords.forEach { breakWord ->
            val index = text.indexOf(breakWord, currentPos)
            if (index != -1) {
                positions.add(index + breakWord.length)
                currentPos = index + breakWord.length
            }
        }
        return positions
    }

    private fun showNextTextPart(
        textView: TextView,
        guideView: View,
        onAnimationEnd: () -> Unit
    ) {
        if (currentPosition >= fullText.length && !isAnimatingNextPage && !isReturningToFirstPage) {
            guideView.visibility = View.INVISIBLE
            Log.d("SentenceReverse", "Text ended, stopping animation")
            animator?.cancel()
            textView.text = currentPartText
            onAnimationEnd()
            return
        }

        val currentBreakWords = reverseBreakWords[selectedTextIndex]
        val breakWord = if (breakWordIndex < currentBreakWords.size) currentBreakWords[breakWordIndex] else ""
        val breakPosition = if (breakWord.isNotEmpty() && !isAnimatingNextPage && !isReturningToFirstPage) {
            val index = fullText.indexOf(breakWord, currentPosition)
            if (index == -1) {
                Log.e("SentenceReverse", "Break word '$breakWord' not found from position $currentPosition")
                fullText.length
            } else {
                index + breakWord.length
            }
        } else {
            fullText.length
        }

        currentPartText = fullText.substring(currentPosition, breakPosition.coerceAtMost(fullText.length)).trim()
        if (!isAnimatingNextPage && !isReturningToFirstPage) {
            if (breakWordIndex >= previousParts.size) {
                previousParts.add(currentPartText)
            } else {
                previousParts[breakWordIndex] = currentPartText
            }
        }
        sentences = parseSentences(currentPartText)
        sentenceStartIndices = calculateSentenceStartIndices(currentPartText, sentences)

        if (!isAnimatingNextPage && !isReturningToFirstPage) {
            currentSentenceIndex = 0
            Log.d("SentenceReverse", "Reset currentSentenceIndex to 0 for new page")
        }
        val currentSentence = sentences.getOrNull(currentSentenceIndex)
        if (isReturningToFirstPage && currentSentence != null) {
            currentWordIndexInSentence = currentSentence.size - 1
            isReturningToFirstPage = false
            Log.d("SentenceReverse", "Returning to first page, set currentWordIndexInSentence=${currentWordIndexInSentence}, currentSentence=${currentSentence.joinToString(" ")}")
        } else {
            currentWordIndexInSentence = if (currentSentence.isNullOrEmpty()) -1 else currentSentence.size - 1
        }

        Log.d("SentenceReverse", "Showing part: startPosition=$currentPosition, endPosition=$breakPosition, breakWord='$breakWord', text='$currentPartText', isAnimatingNextPage=$isAnimatingNextPage, isReturningToFirstPage=$isReturningToFirstPage")
        Log.d("SentenceReverse", "Previous parts: ${previousParts.joinToString(" | ")}")
        Log.d("SentenceReverse", "Sentences: ${sentences.map { it.joinToString(" ") }}")
        Log.d("SentenceReverse", "Sentence start indices: $sentenceStartIndices")
        Log.d("SentenceReverse", "Initial currentSentenceIndex=$currentSentenceIndex, currentWordIndexInSentence=$currentWordIndexInSentence, currentSentence=${currentSentence?.joinToString(" ")}")
        Log.d("SentenceReverse", "Current full sentence words: $currentFullSentenceWords")
        Log.d("SentenceReverse", "First page words read: $firstPageWordsRead")
        if (currentSentence != null && currentWordIndexInSentence >= 0) {
            Log.d("SentenceReverse", "Starting with word: '${currentSentence[currentWordIndexInSentence]}' at position $currentWordIndexInSentence")
        }

        textView.text = currentPartText

        textView.post {
            animateNextWord(textView, guideView, onAnimationEnd)
        }
    }

    private fun parseSentences(text: String): List<List<String>> {
        val sentences = mutableListOf<List<String>>()
        val wordRegex = Regex("""\b\w+\b""")

        if (isAnimatingNextPage && currentFullSentenceWords.isNotEmpty()) {
            val words = wordRegex.findAll(text)
                .map { it.value }
                .filter { it.isNotEmpty() && currentFullSentenceWords.contains(it) }
                .toList()
            if (words.isNotEmpty()) {
                sentences.add(words)
                Log.d("SentenceReverse", "Parsed continuation sentence: ${words.joinToString(" ")} (word count: ${words.size})")
                return sentences
            } else {
                Log.w("SentenceReverse", "No matching words from full sentence in text: $text")
            }
        }

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
        Log.d("SentenceReverse", "animateNextWord: currentSentenceIndex=$currentSentenceIndex, currentWordIndexInSentence=$currentWordIndexInSentence, isAnimatingNextPage=$isAnimatingNextPage, isReturningToFirstPage=$isReturningToFirstPage, fullSentenceWords=$currentFullSentenceWords")

        if (currentSentenceIndex >= sentences.size && !isAnimatingNextPage && !isReturningToFirstPage) {
            if (breakWordIndex + 1 < pagePositions.size) {
                currentPosition = pagePositions[breakWordIndex + 1]
                breakWordIndex++
                Log.d("SentenceReverse", "Page ended, moving to next page, new currentPosition=$currentPosition, breakWordIndex=$breakWordIndex")
                showNextTextPart(textView, guideView, onAnimationEnd)
            } else {
                Log.d("SentenceReverse", "No more pages to display, ending animation")
                guideView.visibility = View.INVISIBLE
                animator?.cancel()
                textView.text = currentPartText
                onAnimationEnd()
            }
            return
        }

        val currentSentence = sentences.getOrNull(currentSentenceIndex)
        if (currentSentence == null || currentSentence.isEmpty() || currentWordIndexInSentence < 0) {
            Log.d("SentenceReverse", "Checking sentence end: currentSentence=${currentSentence?.joinToString(" ")}, currentWordIndexInSentence=$currentWordIndexInSentence, isAnimatingNextPage=$isAnimatingNextPage")
            if (isAnimatingNextPage && currentWordIndexInSentence < 0) {
                val allWordsRead = currentFullSentenceWords.all { word ->
                    firstPageWordsRead.contains(word) || currentSentence?.contains(word) == true
                }
                if (allWordsRead || currentFullSentenceWords.isEmpty()) {
                    isAnimatingNextPage = false
                    isReturningToFirstPage = true
                    breakWordIndex = returnToPageIndex
                    currentPosition = pagePositions.getOrNull(breakWordIndex) ?: currentPosition
                    currentSentenceIndex = returnToSentenceIndex
                    Log.d("SentenceReverse", "Split sentence completed, returning to page $breakWordIndex, position $currentPosition, sentence $currentSentenceIndex, sentenceText=${sentences.getOrNull(currentSentenceIndex)?.joinToString(" ")}, firstPageWordsRead=$firstPageWordsRead")
                    showNextTextPart(textView, guideView, onAnimationEnd)
                    return
                } else {
                    Log.w("SentenceReverse", "Not all words read, continuing on current page")
                }
            }

            if (currentWordIndexInSentence < 0 && isSentenceSplitAcrossNextPage()) {
                Log.d("SentenceReverse", "Sentence split detected, moving to next page")
                animateSplitSentence(textView, guideView, onAnimationEnd)
                return
            }

            currentSentenceIndex++
            val nextSentence = sentences.getOrNull(currentSentenceIndex)
            currentWordIndexInSentence = if (nextSentence.isNullOrEmpty()) -1 else nextSentence.size - 1
            if (!isAnimatingNextPage && currentFullSentenceWords.isEmpty()) {
                currentFullSentenceWords = emptyList()
                firstPageWordsRead = emptyList()
            }
            Log.d("SentenceReverse", "Moving to next sentence: currentSentenceIndex=$currentSentenceIndex, currentWordIndexInSentence=$currentWordIndexInSentence, nextSentence=${nextSentence?.joinToString(" ")}")
            if (nextSentence != null && currentWordIndexInSentence >= 0) {
                Log.d("SentenceReverse", "Starting with word: '${nextSentence[currentWordIndexInSentence]}' at position $currentWordIndexInSentence")
            }
            textView.post { animateNextWord(textView, guideView, onAnimationEnd) }
            return
        }

        if (currentWordIndexInSentence == 0 && isSentenceSplitAcrossNextPage()) {
            Log.d("SentenceReverse", "Reached first word of sentence, checking for split to next page")
            animateSplitSentence(textView, guideView, onAnimationEnd)
            return
        }

        if (!isAnimatingNextPage && !isReturningToFirstPage) {
            firstPageWordsRead = currentSentence.take(currentWordIndexInSentence + 1)
            Log.d("SentenceReverse", "Updated firstPageWordsRead: $firstPageWordsRead")
        }

        Log.d("SentenceReverse", "Starting sentence: ${currentSentence.joinToString(" ")} (word count: ${currentSentence.size})")
        Log.d("SentenceReverse", "Current word: '${currentSentence[currentWordIndexInSentence]}' at position $currentWordIndexInSentence")

        textView.post {
            highlightWord(textView)
            startWordAnimation(textView, guideView, onAnimationEnd)
        }
    }

    private fun isSentenceSplitAcrossNextPage(): Boolean {
        if (breakWordIndex >= pagePositions.size - 1) {
            Log.d("SentenceReverse", "No split: on last page")
            return false
        }

        val sentenceStartInPart = sentenceStartIndices.getOrNull(currentSentenceIndex) ?: 0
        currentSentenceStartIndexInFullText = currentPosition + sentenceStartInPart
        val nextPageStartPosition = pagePositions.getOrNull(breakWordIndex + 1) ?: fullText.length

        val sentenceRegex = Regex("([^.!?()]+[.!?])")
        val allSentences = sentenceRegex.findAll(fullText).toList()
        val currentSentenceMatch = allSentences.find { match ->
            match.range.start <= currentSentenceStartIndexInFullText && currentSentenceStartIndexInFullText <= match.range.endInclusive
        }

        if (currentSentenceMatch == null) {
            Log.w("SentenceReverse", "No matching sentence found for index $currentSentenceStartIndexInFullText")
            return false
        }

        val wordRegex = Regex("""\b\w+\b""")
        currentFullSentenceWords = wordRegex.findAll(currentSentenceMatch.value)
            .map { it.value }
            .filter { it.isNotEmpty() }
            .toList()
        Log.d("SentenceReverse", "Set currentFullSentenceWords: $currentFullSentenceWords")

        val isSplit = currentSentenceMatch.range.endInclusive >= nextPageStartPosition
        Log.d("SentenceReverse", "Checking split: currentSentenceStartIndexInFullText=$currentSentenceStartIndexInFullText, nextPageStartPosition=$nextPageStartPosition, currentSentence='${currentSentenceMatch.value}', isSplit=$isSplit")
        return isSplit
    }

    private fun animateSplitSentence(
        textView: TextView,
        guideView: View,
        onAnimationEnd: () -> Unit
    ) {
        if (breakWordIndex >= pagePositions.size - 1) {
            Log.d("SentenceReverse", "No next page to animate split sentence")
            currentSentenceIndex++
            val nextSentence = sentences.getOrNull(currentSentenceIndex)
            currentWordIndexInSentence = if (nextSentence.isNullOrEmpty()) -1 else nextSentence.size - 1
            currentFullSentenceWords = emptyList()
            firstPageWordsRead = emptyList()
            Log.d("SentenceReverse", "No next page, moving to next sentence: currentSentenceIndex=$currentSentenceIndex, currentWordIndexInSentence=$currentWordIndexInSentence")
            textView.post { animateNextWord(textView, guideView, onAnimationEnd) }
            return
        }

        returnToPageIndex = breakWordIndex
        returnToSentenceIndex = currentSentenceIndex
        isAnimatingNextPage = true
        breakWordIndex++
        currentPosition = pagePositions.getOrNull(breakWordIndex) ?: fullText.length

        val currentBreakWords = reverseBreakWords[selectedTextIndex]
        val breakWord = if (breakWordIndex < currentBreakWords.size) currentBreakWords[breakWordIndex] else ""
        val breakPosition = if (breakWord.isNotEmpty()) {
            val index = fullText.indexOf(breakWord, currentPosition)
            if (index == -1) fullText.length else index + breakWord.length
        } else {
            fullText.length
        }
        currentPartText = fullText.substring(currentPosition, breakPosition.coerceAtMost(fullText.length)).trim()
        if (breakWordIndex >= previousParts.size) {
            previousParts.add(currentPartText)
        } else {
            previousParts[breakWordIndex] = currentPartText
        }

        sentences = parseSentences(currentPartText)
        sentenceStartIndices = calculateSentenceStartIndices(currentPartText, sentences)

        val nextPageWords = currentFullSentenceWords.filter { word ->
            val wordIndex = fullText.indexOf(word, currentPosition)
            wordIndex >= currentPosition && wordIndex < breakPosition
        }

        if (nextPageWords.isEmpty()) {
            Log.e("SentenceReverse", "No words from sentence '$currentFullSentenceWords' found on next page: $currentPartText")
            isAnimatingNextPage = false
            isReturningToFirstPage = true
            breakWordIndex = returnToPageIndex
            currentPosition = pagePositions.getOrNull(breakWordIndex) ?: currentPosition
            currentSentenceIndex = returnToSentenceIndex
            Log.d("SentenceReverse", "No matching words, returning to page $breakWordIndex, position $currentPosition, sentence $currentSentenceIndex")
            showNextTextPart(textView, guideView, onAnimationEnd)
            return
        }

        currentSentenceIndex = sentences.indexOfFirst { sentence ->
            sentence.any { word -> nextPageWords.contains(word) }
        }

        if (currentSentenceIndex < 0 || sentences.getOrNull(currentSentenceIndex)?.isEmpty() != false) {
            Log.w("SentenceReverse", "No matching sentence found in next part: $currentPartText, nextPageWords=$nextPageWords")
            currentSentenceIndex = 0
            val firstSentence = sentences.getOrNull(currentSentenceIndex)
            currentWordIndexInSentence = if (firstSentence.isNullOrEmpty()) -1 else firstSentence.size - 1
            Log.d("SentenceReverse", "Falling back to first sentence: currentSentenceIndex=$currentSentenceIndex, currentWordIndexInSentence=$currentWordIndexInSentence")
        } else {
            currentWordIndexInSentence = sentences[currentSentenceIndex].size - 1
        }

        textView.text = currentPartText
        Log.d("SentenceReverse", "Moved to next page for split sentence: fullSentenceWords='$currentFullSentenceWords', nextPageText='$currentPartText', nextPageWords=$nextPageWords, firstPageWordsRead=$firstPageWordsRead")
        Log.d("SentenceReverse", "Starting with word: '${sentences.getOrNull(currentSentenceIndex)?.getOrNull(currentWordIndexInSentence) ?: "none"}' at position $currentWordIndexInSentence, sentence=${sentences.getOrNull(currentSentenceIndex)?.joinToString(" ")}")

        textView.post {
            animateNextWord(textView, guideView, onAnimationEnd)
        }
    }

    private fun highlightWord(textView: TextView) {
        val spannable = SpannableString(currentPartText)
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
            currentPartText.length
        }
        val wordStartIndex = findWordStartIndex(word, sentenceStartIndex, sentenceEndIndex, currentSentence, currentWordIndexInSentence)

        if (wordStartIndex >= 0 && wordStartIndex < currentPartText.length) {
            val endIndex = wordStartIndex + word.length
            if (endIndex <= currentPartText.length) {
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
        if (startIndex < 0 || startIndex >= currentPartText.length || endIndex > currentPartText.length || startIndex >= endIndex) {
            Log.e("SentenceReverse", "Invalid indices: startIndex=$startIndex, endIndex=$endIndex")
            return -1
        }

        val sentenceText = currentPartText.substring(startIndex, endIndex)
        Log.d("SentenceReverse", "Current sentence text: '$sentenceText'")
        Log.d("SentenceReverse", "Current sentence: ${sentence.joinToString(" ")}")

        var currentIndex = startIndex
        var currentWordIdx = 0
        while (currentIndex < endIndex && currentWordIdx <= wordPosition) {
            while (currentIndex < endIndex && !currentPartText[currentIndex].isLetterOrDigit()) {
                currentIndex++
            }
            if (currentIndex >= endIndex) break

            val currentWord = sentence[currentWordIdx]
            val isWordStart = currentIndex == 0 || !currentPartText[currentIndex - 1].isLetterOrDigit()
            val wordEnd = currentIndex + currentWord.length
            val isWordEnd = wordEnd >= currentPartText.length || !currentPartText[wordEnd].isLetterOrDigit()
            if (isWordStart && isWordEnd && currentPartText.substring(currentIndex, wordEnd) == currentWord) {
                if (currentWordIdx == wordPosition) {
                    return currentIndex
                }
                currentIndex = wordEnd
                currentWordIdx++
            } else {
                while (currentIndex < endIndex && currentPartText[currentIndex].isLetterOrDigit()) {
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
            currentPartText.length
        }
        val wordStartIndex = findWordStartIndex(word, sentenceStartIndex, sentenceEndIndex, currentSentence, currentWordIndexInSentence)

        if (wordStartIndex < 0 || wordStartIndex >= currentPartText.length) {
            Log.e("SentenceReverse", "Invalid wordStartIndex: $wordStartIndex for word: '$word'")
            textView.postDelayed({ animateNextWord(textView, guideView, onAnimationEnd) }, 200)
            return
        }

        val wordEndIndex = wordStartIndex + word.length
        if (wordEndIndex > currentPartText.length) {
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
        val lineY = layout.getLineTop(startLine).toFloat()

        Log.d("SentenceReverse", "Animating word: '$word' at position $currentWordIndexInSentence, startX=$startX, endX=$endX, lineY=$lineY, startLine=$startLine, endLine=$endLine")

        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 300L
            addUpdateListener { animation ->
                val fraction = animation.animatedValue as Float
                val currentX = startX + (endX - startX) * fraction
                guideView.translationX = currentX - (guideView.width / 2) + textView.left
                guideView.translationY = lineY + textView.top.toFloat()
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