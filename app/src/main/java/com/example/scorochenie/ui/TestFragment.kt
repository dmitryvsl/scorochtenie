package com.example.scorochenie.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.scorochenie.domain.Technique
import com.example.scorochenie.domain.TextResources
import com.example.scorochenie.databinding.FragmentTestBinding
import org.json.JSONObject
import android.util.Log
import com.example.scorochenie.domain.TechniqueType

class TestFragment : Fragment() {

    companion object {
        private const val ARG_TEXT_INDEX = "text_index"
        private const val ARG_TECHNIQUE_TYPE = "technique_type"
        private const val ARG_DURATION_PER_WORD = "duration_per_word"

        fun newInstance(
            textIndex: Int,
            techniqueType: TechniqueType,
            durationPerWord: Long
        ): TestFragment {
            return TestFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_TEXT_INDEX, textIndex)
                    putSerializable(ARG_TECHNIQUE_TYPE, techniqueType)
                    putLong(ARG_DURATION_PER_WORD, durationPerWord)
                }
            }
        }
    }

    private var _binding: FragmentTestBinding? = null
    private val binding get() = _binding!!
    private var score = 0
    private var currentTextIndex = 0
    private lateinit var techniqueType: TechniqueType
    private var durationPerWord: Long = 400L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        parseArgs()
        displayQuestion(0)
        binding.btnSubmit.setOnClickListener {
            checkAnswer()
        }
    }

    private fun parseArgs() {
        currentTextIndex = arguments?.getInt(ARG_TEXT_INDEX, 0) ?: 0
        techniqueType = arguments?.getSerializable(ARG_TECHNIQUE_TYPE).let {
            it as? TechniqueType
                ?: throw RuntimeException("Can't find argument '${ARG_TECHNIQUE_TYPE}' in fragment 'TestFragment'")
        }
        durationPerWord = arguments?.getLong(ARG_DURATION_PER_WORD) ?: 400L
    }

    private fun displayQuestion(index: Int) {

        val questions = when (techniqueType) {
            TechniqueType.DiagonalReading -> TextResources.getDiagonalTexts()
                .getOrNull(currentTextIndex)?.questionsAndAnswers

            TechniqueType.KeywordSearch -> TextResources.getKeywordTexts()
                .getOrNull(currentTextIndex)?.questionsAndAnswers

            else -> TextResources.getOtherTexts()[techniqueType.displayName]?.getOrNull(
                currentTextIndex
            )?.questionsAndAnswers
        }

        if (questions.isNullOrEmpty()) {
            Log.e(
                "TestFragment",
                "No questions found for technique='${techniqueType.displayName}', textIndex=$currentTextIndex"
            )
            binding.tvQuestionHeader.visibility = View.GONE
            binding.questionText.text = "Ошибка: вопросы для этой техники недоступны."
            binding.radioGroup.visibility = View.GONE
            binding.btnSubmit.visibility = View.GONE
            return
        }

        if (index < questions.size) {
            val questionPair = questions[index]
            binding.questionText.text = questionPair.first
            binding.questionText.tag = Pair(index, questionPair.second[0])

            binding.tvQuestionHeader.text = "Вопрос ${index + 1}"

            binding.radioGroup.removeAllViews()
            val options = questionPair.second.shuffled()
            options.forEach { option ->
                val radioButton = RadioButton(context).apply {
                    text = option
                    id = View.generateViewId()
                    textSize = 16f
                    setTextColor(context?.let { ContextCompat.getColor(it, android.R.color.black) }
                        ?: android.graphics.Color.WHITE)
                }
                binding.radioGroup.addView(radioButton)
            }
        } else {
            showResult()
        }
    }

    private fun checkAnswer() {
        val selectedRadioButtonId = binding.radioGroup.checkedRadioButtonId
        if (selectedRadioButtonId == -1) {
            Toast.makeText(context, "Выберите ответ", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedRadioButton =
            binding.radioGroup.findViewById<RadioButton>(selectedRadioButtonId)
        val userAnswer = selectedRadioButton.text.toString().lowercase()
        val correctAnswer = (binding.questionText.tag as? Pair<*, *>)?.second as? String ?: ""
        if (userAnswer == correctAnswer.lowercase()) {
            score++
        }

        binding.radioGroup.clearCheck()
        displayQuestion(((binding.questionText.tag as? Pair<*, *>)?.first as? Int ?: 0) + 1)
    }

    private fun showResult() {

        val totalQuestions = when (techniqueType) {
            TechniqueType.DiagonalReading -> TextResources.getDiagonalTexts()
                .getOrNull(currentTextIndex)?.questionsAndAnswers?.size

            TechniqueType.KeywordSearch -> TextResources.getKeywordTexts()
                .getOrNull(currentTextIndex)?.questionsAndAnswers?.size

            else -> TextResources.getOtherTexts()[techniqueType.displayName]?.getOrNull(
                currentTextIndex
            )?.questionsAndAnswers?.size
        } ?: 0

        binding.tvQuestionHeader.visibility = View.GONE
        binding.questionText.text = "Тест завершён! Ваш результат: $score из $totalQuestions"
        binding.radioGroup.visibility = View.GONE
        binding.btnSubmit.visibility = View.GONE

        saveTestResult(techniqueType.displayName, totalQuestions)
    }

    private fun saveTestResult(normalizedTechniqueName: String, totalQuestions: Int) {
        val sharedPreferences =
            requireContext().getSharedPreferences("TestResults", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val key = "result_$normalizedTechniqueName"

        val existingResultJson = sharedPreferences.getString(key, null)
        var shouldSave = true

        if (existingResultJson != null) {
            try {
                val existingResult = JSONObject(existingResultJson)
                val existingScore = existingResult.getInt("score")
                val existingDuration = existingResult.getLong("durationPerWord")

                if (score < existingScore || (score == existingScore && durationPerWord >= existingDuration)) {
                    shouldSave = false
                }
            } catch (e: Exception) {
                Log.e(
                    "TestFragment",
                    "Failed to parse existing result JSON: $existingResultJson",
                    e
                )
            }
        }

        if (shouldSave) {
            val timestamp = System.currentTimeMillis()
            val resultJson = """
                {
                    "techniqueName": "$normalizedTechniqueName",
                    "durationPerWord": $durationPerWord,
                    "score": $score,
                    "totalQuestions": $totalQuestions,
                    "timestamp": $timestamp
                }
            """
            editor.putString(key, resultJson)
            editor.apply()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}