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
    private var sentences: List<List<String>> = emptyList() // Список предложений, каждое — список слов
    private var sentenceStartIndices: List<Int> = emptyList() // Индексы начала предложений в currentPartText
    private var previousParts: MutableList<String> = mutableListOf() // Хранит текст предыдущих страниц
    private var currentSentenceStartIndexInFullText: Int = -1 // Индекс начала текущего предложения в fullText
    private var isAnimatingPreviousPage: Boolean = false // Флаг для анимации на предыдущей странице

    // Слова-прерыватели для перевёрнутого текста
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
        isAnimatingPreviousPage = false

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
        if (currentPosition >= fullText.length) {
            guideView.visibility = View.INVISIBLE
            Log.d("SentenceReverse", "Text ended, stopping animation")
            animator?.cancel()
            val currentText = textView.text.toString()
            textView.text = currentText
            onAnimationEnd()
            return
        }

        val currentBreakWords = reverseBreakWords[selectedTextIndex]
        val breakWord = if (breakWordIndex < currentBreakWords.size) currentBreakWords[breakWordIndex] else ""
        val breakPosition = if (breakWord.isNotEmpty()) {
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

        currentPartText = fullText.substring(currentPosition, breakPosition).trim()
        if (!isAnimatingPreviousPage) {
            previousParts.add(currentPartText)
        }
        sentences = parseSentences(currentPartText)
        sentenceStartIndices = calculateSentenceStartIndices(currentPartText, sentences)
        currentSentenceIndex = 0
        currentWordIndexInSentence = sentences.getOrNull(0)?.size?.minus(1) ?: -1

        textView.text = currentPartText

        Log.d("SentenceReverse", "Showing part: startPosition=$currentPosition, endPosition=$breakPosition, breakWord='$breakWord', text='$currentPartText'")
        Log.d("SentenceReverse", "Previous parts: ${previousParts.joinToString(" | ")}")

        textView.post {
            animateNextWord(textView, guideView, onAnimationEnd)
        }
    }

    // Разбивает текст на предложения, каждое — список слов
    private fun parseSentences(text: String): List<List<String>> {
        val sentenceRegex = Regex("([^.!?]+[.!?])")
        val sentences = mutableListOf<List<String>>()
        sentenceRegex.findAll(text).forEach { matchResult ->
            val sentenceText = matchResult.value.trim()
            val words = sentenceText.split("\\s+".toRegex())
                .filter { it.isNotEmpty() && it.any { c -> c.isLetterOrDigit() } }
            sentences.add(words)
        }
        return sentences
    }

    // Вычисляет индексы начала каждого предложения в currentPartText
    private fun calculateSentenceStartIndices(text: String, sentences: List<List<String>>): List<Int> {
        val indices = mutableListOf<Int>()
        var currentIndex = 0
        sentences.forEach { sentence ->
            indices.add(currentIndex)
            sentence.forEach { word ->
                currentIndex += word.length
                if (currentIndex < text.length && text[currentIndex] == ' ') {
                    currentIndex++
                }
            }
            while (currentIndex < text.length && !text[currentIndex].isLetterOrDigit()) {
                currentIndex++
            }
        }
        return indices
    }

    // Переворачивает слова в каждом предложении
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
        if (currentSentenceIndex >= sentences.size && !isAnimatingPreviousPage) {
            currentPosition += currentPartText.length + 1
            breakWordIndex++
            Log.d("SentenceReverse", "Part ended, moving to next part, new currentPosition=$currentPosition, breakWordIndex=$breakWordIndex")
            showNextTextPart(textView, guideView, onAnimationEnd)
            return
        }

        val currentSentence = sentences[currentSentenceIndex]
        if (currentWordIndexInSentence < 0) {
            if (isAnimatingPreviousPage) {
                // Завершили анимацию на предыдущей странице, возвращаемся
                isAnimatingPreviousPage = false
                currentPosition -= (currentPartText.length + 1)
                breakWordIndex--
                Log.d("SentenceReverse", "Split sentence completed, returning to next part, currentPosition=$currentPosition, breakWordIndex=$breakWordIndex")
                showNextTextPart(textView, guideView, onAnimationEnd)
                return
            }

            // Проверяем, разорвано ли текущее предложение
            if (isSentenceSplitAcrossPages()) {
                Log.d("SentenceReverse", "Sentence split detected, returning to previous page")
                animateSplitSentence(textView, guideView, onAnimationEnd)
                return
            }

            currentSentenceIndex++
            currentWordIndexInSentence = sentences.getOrNull(currentSentenceIndex)?.size?.minus(1) ?: -1
            animateNextWord(textView, guideView, onAnimationEnd)
            return
        }

        highlightWord(textView)
        startWordAnimation(textView, guideView, onAnimationEnd)
    }

    // Проверяет, началось ли текущее предложение на предыдущей странице
    private fun isSentenceSplitAcrossPages(): Boolean {
        if (previousParts.size <= 1 || currentSentenceIndex >= sentences.size) return false

        // Находим индекс начала текущего предложения в fullText
        val sentenceStartInPart = sentenceStartIndices[currentSentenceIndex]
        currentSentenceStartIndexInFullText = currentPosition - currentPartText.length + sentenceStartInPart

        // Получаем полное предложение из fullText
        val sentenceRegex = Regex("([^.!?]+[.!?])")
        val allSentences = sentenceRegex.findAll(fullText).toList()
        val currentSentenceInFullText = allSentences.find { match ->
            match.range.start <= currentSentenceStartIndexInFullText && currentSentenceStartIndexInFullText < match.range.endInclusive + 1
        }?.value ?: return false

        // Проверяем, начинается ли это предложение на предыдущей странице
        val previousPartEndPosition = currentPosition - currentPartText.length
        return currentSentenceInFullText.isNotEmpty() && currentSentenceStartIndexInFullText < previousPartEndPosition
    }

    // Анимирует оставшуюся часть разорванного предложения на предыдущей странице
    private fun animateSplitSentence(
        textView: TextView,
        guideView: View,
        onAnimationEnd: () -> Unit
    ) {
        if (previousParts.size < 2) {
            currentSentenceIndex++
            currentWordIndexInSentence = sentences.getOrNull(currentSentenceIndex)?.size?.minus(1) ?: -1
            animateNextWord(textView, guideView, onAnimationEnd)
            return
        }

        // Показываем предыдущую страницу
        isAnimatingPreviousPage = true
        currentPartText = previousParts[previousParts.size - 2]
        sentences = parseSentences(currentPartText)
        sentenceStartIndices = calculateSentenceStartIndices(currentPartText, sentences)

        // Находим последнее предложение предыдущей страницы
        val lastSentenceIndex = sentences.size - 1
        currentSentenceIndex = lastSentenceIndex
        currentWordIndexInSentence = sentences[lastSentenceIndex].size - 1

        textView.text = currentPartText
        Log.d("SentenceReverse", "Returned to previous page for split sentence: text='$currentPartText'")

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

        val currentSentence = sentences[currentSentenceIndex]
        val word = currentSentence[currentWordIndexInSentence]
        val sentenceStartIndex = sentenceStartIndices[currentSentenceIndex]
        val wordStartIndex = findWordStartIndex(word, sentenceStartIndex)

        if (wordStartIndex >= 0 && wordStartIndex < currentPartText.length) {
            val endIndex = wordStartIndex + word.length
            spannable.setSpan(
                BackgroundColorSpan(Color.YELLOW),
                wordStartIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            Log.d("SentenceReverse", "Highlighting word: '$word', start=$wordStartIndex, end=$endIndex")
        } else {
            Log.e("SentenceReverse", "Invalid word start index: $wordStartIndex for word: '$word'")
        }

        textView.text = spannable
    }

    private fun findWordStartIndex(word: String, startIndex: Int): Int {
        var currentIndex = startIndex
        val tokens = currentPartText.substring(startIndex).split("\\s+".toRegex())
        var tokenIndex = 0
        while (tokenIndex < tokens.size) {
            val token = tokens[tokenIndex]
            if (token == word) {
                return currentIndex
            }
            if (token.any { it.isLetterOrDigit() }) {
                currentIndex += token.length
                if (currentIndex < currentPartText.length && currentPartText[currentIndex] == ' ') {
                    currentIndex++
                }
            } else {
                currentIndex += token.length
            }
            tokenIndex++
        }
        return -1
    }

    private fun startWordAnimation(
        textView: TextView,
        guideView: View,
        onAnimationEnd: () -> Unit
    ) {
        guideView.visibility = View.VISIBLE
        animator?.cancel()

        val layout = textView.layout ?: return
        val currentSentence = sentences[currentSentenceIndex]
        val word = currentSentence[currentWordIndexInSentence]
        val sentenceStartIndex = sentenceStartIndices[currentSentenceIndex]
        val wordStartIndex = findWordStartIndex(word, sentenceStartIndex)

        if (wordStartIndex < 0 || wordStartIndex >= currentPartText.length) {
            Log.e("SentenceReverse", "Invalid wordStartIndex: $wordStartIndex for word: '$word'")
            currentWordIndexInSentence--
            animateNextWord(textView, guideView, onAnimationEnd)
            return
        }

        val wordEndIndex = wordStartIndex + word.length
        if (wordEndIndex > currentPartText.length) {
            Log.e("SentenceReverse", "Invalid wordEndIndex: $wordEndIndex for word: '$word'")
            currentWordIndexInSentence--
            animateNextWord(textView, guideView, onAnimationEnd)
            return
        }

        val startLine = layout.getLineForOffset(wordStartIndex)
        val endLine = layout.getLineForOffset(wordEndIndex)
        val startX = layout.getPrimaryHorizontal(wordStartIndex)
        val endX = layout.getPrimaryHorizontal(wordEndIndex)
        val lineY = layout.getLineTop(startLine).toFloat()

        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 30L
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
                    animateNextWord(textView, guideView, onAnimationEnd)
                }
            )
            start()
        }
    }

    // Внутренний класс для обработки слов и пунктуации
    data class WordChunk(val word: String, val punctuation: MutableList<String>)
}