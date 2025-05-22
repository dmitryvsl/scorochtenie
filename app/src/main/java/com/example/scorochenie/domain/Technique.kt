package com.example.scorochenie.domain

import android.text.SpannableString
import android.view.View
import android.widget.TextView

open class Technique(val name: String, val displayName: String) {
    open val description: SpannableString
        get() = SpannableString("Описание для $displayName недоступно")

    open fun startAnimation(
        textView: TextView,
        guideView: View,
        durationPerWord: Long,
        selectedTextIndex: Int,
        onAnimationEnd: () -> Unit
    ) {
        textView.text = "Анимация для $displayName недоступна"
        guideView.visibility = View.INVISIBLE
        onAnimationEnd()
    }

    open fun cancelAnimation() {}

    companion object {
        private val techniqueNames = mapOf(
            "BlockReadingTechnique" to "Чтение \"блоками\"",
            "DiagonalReadingTechnique" to "Чтение по диагонали",
            "KeywordSearchTechnique" to "Поиск ключевых слов",
            "PointerMethodTechnique" to "Метод \"указки\"",
            "SentenceReverseTechnique" to "Предложения наоборот",
            "WordReverseTechnique" to "Слова наоборот"
        )

        fun getDisplayName(name: String): String {
            return techniqueNames[name] ?: name
        }

        fun createTechnique(name: String): ReadingTechnique {
            val displayName = getDisplayName(name)
            return when (name) {
                "BlockReadingTechnique" -> BlockReadingTechnique()
                "DiagonalReadingTechnique" -> DiagonalReadingTechnique()
                "KeywordSearchTechnique" -> KeywordSearchTechnique()
                "PointerMethodTechnique" -> PointerMethodTechnique()
                "SentenceReverseTechnique" -> SentenceReverseTechnique()
                "WordReverseTechnique" -> WordReverseTechnique()
                else -> object : ReadingTechnique("UnknownTechnique", displayName) {
                    override val description: SpannableString
                        get() = SpannableString("Описание для этой техники недоступно")

                    override fun startAnimation(
                        textView: TextView,
                        guideView: View,
                        durationPerWord: Long,
                        selectedTextIndex: Int,
                        onAnimationEnd: () -> Unit
                    ) {
                        textView.text = "Анимация недоступна"
                        guideView.visibility = View.INVISIBLE
                        onAnimationEnd()
                    }

                    override fun cancelAnimation() {
                        // Пустая реализация для неизвестной техники
                    }
                }
            }
        }
    }
}