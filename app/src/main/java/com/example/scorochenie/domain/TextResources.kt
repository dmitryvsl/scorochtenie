package com.example.scorochenie.domain

import android.content.Context
import com.example.scorochenie.R
import org.xmlpull.v1.XmlPullParser

// Базовый класс для текстов, только текст и вопросы
data class TextData(
    val text: String,
    val questionsAndAnswers: List<Pair<String, List<String>>>
)

// Текст для "Чтение по диагонали" с breakWords
data class DiagonalTextData(
    val text: String,
    val breakWords: List<String>,
    val questionsAndAnswers: List<Pair<String, List<String>>>
)

// Текст для "Поиск ключевых слов" с keyWords
data class KeywordTextData(
    val text: String,
    val keyWords: List<String>,
    val questionsAndAnswers: List<Pair<String, List<String>>>
)

object TextResources {
    // Списки для хранения текстов
    private var diagonalTexts: List<DiagonalTextData> = emptyList()
    private var keywordTexts: List<KeywordTextData> = emptyList()
    private var otherTexts: Map<String, List<TextData>> = emptyMap()

    // Инициализация текстов из XML
    fun initialize(context: Context) {
        try {
            val parser = context.resources.getXml(R.xml.texts)
            val diagonalList = mutableListOf<DiagonalTextData>()
            val keywordList = mutableListOf<KeywordTextData>()
            val otherMap = mutableMapOf<String, MutableList<TextData>>()

            var currentTechnique: String? = null
            var currentText: StringBuilder? = null
            var currentBreakWords: MutableList<String>? = null
            var currentKeyWords: MutableList<String>? = null
            var currentQuestions: MutableList<Pair<String, List<String>>>? = null
            var currentQuestionText: StringBuilder? = null
            var currentAnswers: MutableList<String>? = null
            var currentCorrectAnswer: String? = null

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "technique" -> {
                                currentTechnique = parser.getAttributeValue(null, "name")
                                otherMap[currentTechnique] = mutableListOf()
                            }
                            "text" -> {
                                currentText = StringBuilder()
                                currentBreakWords = mutableListOf()
                                currentKeyWords = mutableListOf()
                                currentQuestions = mutableListOf()
                            }
                            "content" -> {
                                currentText?.append(parser.nextText().trim())
                            }
                            "breakWords" -> {
                                currentBreakWords = mutableListOf()
                            }
                            "keyWords" -> {
                                currentKeyWords = mutableListOf()
                            }
                            "word" -> {
                                val word = parser.nextText().trim()
                                currentBreakWords?.add(word)
                                currentKeyWords?.add(word)
                            }
                            "questions" -> {
                                currentQuestions = mutableListOf()
                            }
                            "question" -> {
                                currentQuestionText = StringBuilder(parser.getAttributeValue(null, "text"))
                                currentAnswers = mutableListOf()
                            }
                            "answer" -> {
                                val answer = parser.nextText().trim()
                                currentAnswers?.add(answer)
                                if (parser.getAttributeValue(null, "correct") == "true") {
                                    currentCorrectAnswer = answer
                                }
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        when (parser.name) {
                            "text" -> {
                                if (currentTechnique != null && currentText != null && currentQuestions != null) {
                                    when (currentTechnique) {
                                        "Чтение по диагонали" -> {
                                            diagonalList.add(
                                                DiagonalTextData(
                                                    text = currentText.toString(),
                                                    breakWords = currentBreakWords ?: emptyList(),
                                                    questionsAndAnswers = currentQuestions.toList()
                                                )
                                            )
                                        }
                                        "Поиск ключевых слов" -> {
                                            keywordList.add(
                                                KeywordTextData(
                                                    text = currentText.toString(),
                                                    keyWords = currentKeyWords ?: emptyList(),
                                                    questionsAndAnswers = currentQuestions.toList()
                                                )
                                            )
                                        }
                                        else -> {
                                            otherMap[currentTechnique]?.add(
                                                TextData(
                                                    text = currentText.toString(),
                                                    questionsAndAnswers = currentQuestions.toList()
                                                )
                                            )
                                        }
                                    }
                                }
                                currentText = null
                                currentBreakWords = null
                                currentKeyWords = null
                                currentQuestions = null
                            }
                            "question" -> {
                                if (currentQuestionText != null && currentAnswers != null) {
                                    currentQuestions?.add(
                                        currentQuestionText.toString() to currentAnswers.toList()
                                    )
                                }
                                currentQuestionText = null
                                currentAnswers = null
                                currentCorrectAnswer = null
                            }
                        }
                    }
                }
                eventType = parser.next()
            }

            diagonalTexts = diagonalList
            keywordTexts = keywordList
            otherTexts = otherMap
        } catch (e: Exception) {
            e.printStackTrace()
            // В случае ошибки задаем пустые списки
            diagonalTexts = emptyList()
            keywordTexts = emptyList()
            otherTexts = emptyMap()
        }
    }

    // Геттеры для доступа к текстам
    fun getDiagonalTexts(): List<DiagonalTextData> = diagonalTexts
    fun getKeywordTexts(): List<KeywordTextData> = keywordTexts
    fun getOtherTexts(): Map<String, List<TextData>> = otherTexts
}