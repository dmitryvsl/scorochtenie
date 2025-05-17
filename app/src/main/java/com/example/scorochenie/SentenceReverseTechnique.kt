package com.example.scorochenie

import android.animation.ValueAnimator
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.animation.addListener
import kotlin.random.Random

class SentenceReverseTechnique : ReadingTechnique("Предложения наоборот") {
    private var currentWordIndex = 0
    private var selectedTextIndex = 0
    private var fullText: String = ""
    private var animator: ValueAnimator? = null
    private var allWords: List<String> = emptyList()
    private var sentenceBoundaries: List<Int> = emptyList()

    override val description: SpannableString
        get() {
            val text = "Предложения наоборот — это техника скорочтения, направленная на формирование навыка правильного чтения и профилактику «зеркального» чтения. Текст читается, начиная с последнего слова каждого предложения.\n" +
                    "Для применения техники начинайте с последнего слова предложения и двигайтесь к первому.\n" +
                    "Сосредоточьтесь на правильном порядке чтения слов, чтобы улучшить внимание и навыки чтения."
            val spannable = SpannableString(text)
            spannable.setSpan(android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, name.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(android.text.style.StyleSpan(android.graphics.Typeface.BOLD), text.indexOf("начинайте с последнего слова"), text.indexOf("начинайте с последнего слова") + "начинайте с последнего слова".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(android.text.style.StyleSpan(android.graphics.Typeface.BOLD), text.indexOf("правильном порядке чтения"), text.indexOf("правильном порядке чтения") + "правильном порядке чтения".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
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
        allWords = fullText.split("\\s+".toRegex()).filter { it.isNotEmpty() && it.any { c -> c.isLetterOrDigit() } }
        sentenceBoundaries = calculateSentenceBoundaries(originalText)
        currentWordIndex = 0
        textView.gravity = android.view.Gravity.TOP
        textView.text = fullText
        textView.post {
            animateNextWord(textView, guideView, onAnimationEnd)
        }
    }

    // Объявите этот класс ВНУТРИ класса SentenceReverseTechnique (или вне, если хотите использовать в других файлах)
    data class WordChunk(val word: String, val punctuation: MutableList<String>)

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

            // ИЩЕМ ПОСЛЕДНЕЕ СЛОВО
            val wordPattern = Regex("""\b([a-zA-Zа-яА-ЯёЁ]+)\b""")
            val lastWordMatch = wordPattern.findAll(sent).lastOrNull()
            if (lastWordMatch != null) {
                val lastWord = lastWordMatch.value
                val range = lastWordMatch.range
                val correctedLastWord = lastWord.replaceFirstChar { it.lowercaseChar() }
                sent = sent.substring(0, range.start) + correctedLastWord + sent.substring(range.endInclusive + 1)
            }

            // Первая буква предложения — заглавная
            val finalSent = sent.replaceFirstChar { it.uppercaseChar() }

            "$finalSent$endPunct"
        }
    }

    private fun calculateSentenceBoundaries(text: String): List<Int> {
        val sentences = text.split("(?<=[.!?])\\s+".toRegex()).filter { it.isNotEmpty() }
        val boundaries = mutableListOf<Int>()
        var wordCount = 0
        sentences.forEach { sentence ->
            val words = sentence.split("\\s+".toRegex()).filter { it.isNotEmpty() && it.any { c -> c.isLetterOrDigit() } }
            wordCount += words.size
            boundaries.add(wordCount)
        }
        return boundaries
    }

    private fun animateNextWord(
        textView: TextView,
        guideView: View,
        onAnimationEnd: () -> Unit
    ) {
        if (currentWordIndex >= allWords.size) {
            guideView.visibility = View.INVISIBLE
            Log.d("SentenceReverse", "Animation ended")
            animator?.cancel()
            textView.text = fullText
            onAnimationEnd()
            return
        }
        highlightWord(textView)
        startWordAnimation(textView, guideView, onAnimationEnd)
    }

    private fun highlightWord(textView: TextView) {
        val spannable = SpannableString(fullText)
        val existingSpans = spannable.getSpans(0, spannable.length, BackgroundColorSpan::class.java)
        for (span in existingSpans) {
            spannable.removeSpan(span)
        }
        val wordIndexInSentence = getWordIndexInSentence()
        val (startIndex, word) = getWordPosition(wordIndexInSentence)
        if (startIndex >= 0 && startIndex < fullText.length) {
            val endIndex = startIndex + word.length
            spannable.setSpan(
                BackgroundColorSpan(Color.YELLOW),
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            Log.d("SentenceReverse", "Highlighting word: '$word', start=$startIndex, end=$endIndex")
        }
        textView.text = spannable
    }

    private fun getWordIndexInSentence(): Int {
        var sentenceIndex = 0
        for (boundary in sentenceBoundaries) {
            if (currentWordIndex < boundary) {
                val wordsInSentence = if (sentenceIndex == 0) boundary else boundary - sentenceBoundaries[sentenceIndex - 1]
                val positionInSentence = if (sentenceIndex == 0) currentWordIndex else currentWordIndex - sentenceBoundaries[sentenceIndex - 1]
                return wordsInSentence - 1 - positionInSentence
            }
            sentenceIndex++
        }
        return 0
    }

    private fun getWordPosition(wordIndexInSentence: Int): Pair<Int, String> {
        var startIndex = 0
        var sentenceIndex = 0
        var sentenceStart = 0
        for (boundary in sentenceBoundaries) {
            if (currentWordIndex < boundary) {
                val wordsInSentence = if (sentenceIndex == 0) boundary else boundary - sentenceBoundaries[sentenceIndex - 1]
                val targetWordIndex = sentenceStart + (wordsInSentence - 1 - wordIndexInSentence)
                val tokens = fullText.substring(startIndex).split("\\s+".toRegex()).filter { it.isNotEmpty() }
                var tokenIndex = 0
                var localWordIndex = 0
                var currentIndex = startIndex
                while (tokenIndex < tokens.size && localWordIndex <= targetWordIndex) {
                    val token = tokens[tokenIndex]
                    if (token.any { c -> c.isLetterOrDigit() }) {
                        if (localWordIndex == targetWordIndex) {
                            return Pair(currentIndex, token)
                        }
                        localWordIndex++
                    }
                    currentIndex += token.length
                    if (currentIndex < fullText.length && fullText[currentIndex] == ' ') {
                        currentIndex++
                    }
                    tokenIndex++
                }
            }
            sentenceStart = boundary
            sentenceIndex++
        }
        return Pair(-1, "")
    }

    private fun startWordAnimation(
        textView: TextView,
        guideView: View,
        onAnimationEnd: () -> Unit
    ) {
        guideView.visibility = View.VISIBLE
        animator?.cancel()
        val layout = textView.layout ?: return
        val wordIndexInSentence = getWordIndexInSentence()
        val (wordStartIndex, word) = getWordPosition(wordIndexInSentence)
        if (wordStartIndex < 0 || wordStartIndex >= fullText.length) {
            Log.e("SentenceReverse", "Invalid wordStartIndex: $wordStartIndex")
            currentWordIndex++
            animateNextWord(textView, guideView, onAnimationEnd)
            return
        }
        val wordEndIndex = wordStartIndex + word.length
        if (wordEndIndex > fullText.length) {
            Log.e("SentenceReverse", "Invalid wordEndIndex: $wordEndIndex")
            currentWordIndex++
            animateNextWord(textView, guideView, onAnimationEnd)
            return
        }
        val startLine = layout.getLineForOffset(wordStartIndex)
        val endLine = layout.getLineForOffset(wordEndIndex)
        val startX = layout.getPrimaryHorizontal(wordStartIndex)
        val endX = layout.getPrimaryHorizontal(wordEndIndex)
        val lineY = layout.getLineTop(startLine).toFloat()
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
                    currentWordIndex++
                    Log.d("SentenceReverse", "Word animation ended, currentWordIndex=$currentWordIndex")
                    animateNextWord(textView, guideView, onAnimationEnd)
                }
            )
            start()
        }
    }
}