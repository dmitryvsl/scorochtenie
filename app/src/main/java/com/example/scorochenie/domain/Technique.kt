package com.example.scorochenie.domain

import android.text.SpannableString
import android.view.View
import android.widget.TextView

abstract class Technique(protected val techniqueType: TechniqueType){

    abstract val description: SpannableString

    abstract fun startAnimation(
        textView: TextView,
        guideView: View,
        durationPerWord: Long,
        selectedTextIndex: Int,
        onAnimationEnd: () -> Unit
    )

    open fun cancelAnimation() {}

    companion object {

        fun createTechnique(techniqueType: TechniqueType): Technique {
            return when (techniqueType) {
                TechniqueType.BlockReading -> BlockReadingTechnique()
                TechniqueType.DiagonalReading -> DiagonalReadingTechnique()
                TechniqueType.KeywordSearch -> KeywordSearchTechnique()
                TechniqueType.PointerMethod -> PointerMethodTechnique()
                TechniqueType.SentenceReverse -> SentenceReverseTechnique()
                TechniqueType.WordReverse -> WordReverseTechnique()
            }
        }

        fun getAllTechniques(): List<TechniqueType> = TechniqueType.entries
    }
}