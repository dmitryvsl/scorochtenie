package com.example.scorochenie.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.scorochenie.domain.TextResources
import com.example.scorochenie.databinding.FragmentTestBinding
import org.json.JSONObject

class TestFragment : Fragment() {

    companion object {
        private const val ARG_TEXT_INDEX = "textIndex"
        private const val ARG_TECHNIQUE_NAME = "techniqueName"
        private const val ARG_DURATION_PER_WORD = "durationPerWord"

        fun newInstance(textIndex: Int, techniqueName: String, durationPerWord: Long): TestFragment {
            return TestFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_TEXT_INDEX, textIndex)
                    putString(ARG_TECHNIQUE_NAME, techniqueName)
                    putLong(ARG_DURATION_PER_WORD, durationPerWord)
                }
            }
        }
    }

    private var _binding: FragmentTestBinding? = null
    private val binding get() = _binding!!
    private var score = 0
    private var currentTextIndex = 0
    private var techniqueName: String = ""
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

        currentTextIndex = arguments?.getInt(ARG_TEXT_INDEX, 0) ?: 0
        techniqueName = arguments?.getString(ARG_TECHNIQUE_NAME) ?: ""
        durationPerWord = arguments?.getLong(ARG_DURATION_PER_WORD) ?: 400L

        Log.d("TestFragment", "onViewCreated: techniqueName='$techniqueName', currentTextIndex=$currentTextIndex, durationPerWord=$durationPerWord")

        displayQuestion(0)

        binding.btnSubmit.setOnClickListener {
            checkAnswer()
        }
    }

    private fun displayQuestion(index: Int) {
        val normalizedTechniqueName = when (techniqueName) {
            "DiagonalReadingTechnique" -> "Чтение по диагонали"
            "KeywordSearchTechnique" -> "Поиск ключевых слов"
            "BlockReadingTechnique" -> "Чтение блоками"
            "SentenceReverseTechnique" -> "Предложения наоборот"
            "WordReverseTechnique" -> "Слова наоборот"
            "PointerMethodTechnique" -> "Метод указки"
            else -> techniqueName
        }

        Log.d("TestFragment", "displayQuestion: index=$index, normalizedTechniqueName='$normalizedTechniqueName'")

        val questions = when (normalizedTechniqueName) {
            "Чтение по диагонали" -> TextResources.diagonalTexts.getOrNull(currentTextIndex)?.questionsAndAnswers
            "Поиск ключевых слов" -> TextResources.keywordTexts.getOrNull(currentTextIndex)?.questionsAndAnswers
            else -> {
                Log.d("TestFragment", "Attempting to access otherTexts with key: '$normalizedTechniqueName'")
                Log.d("TestFragment", "Available keys in otherTexts: ${TextResources.otherTexts.keys}")
                TextResources.otherTexts[normalizedTechniqueName]?.getOrNull(currentTextIndex)?.questionsAndAnswers
            }
        }

        Log.d("TestFragment", "Questions: size=${questions?.size ?: 0}, questions=$questions")

        if (questions.isNullOrEmpty()) {
            Log.e("TestFragment", "No questions found for technique='$normalizedTechniqueName', textIndex=$currentTextIndex")
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
                    setTextColor(ContextCompat.getColor(context, android.R.color.white))
                }
                binding.radioGroup.addView(radioButton)
            }
            Log.d("TestFragment", "Displayed question $index: '${questionPair.first}', options=$options")
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

        val selectedRadioButton = binding.radioGroup.findViewById<RadioButton>(selectedRadioButtonId)
        val userAnswer = selectedRadioButton.text.toString().lowercase()
        val correctAnswer = (binding.questionText.tag as? Pair<*, *>)?.second as? String ?: ""
        if (userAnswer == correctAnswer.lowercase()) {
            score++
        }

        Log.d("TestFragment", "Checked answer: userAnswer='$userAnswer', correctAnswer='$correctAnswer', score=$score")

        binding.radioGroup.clearCheck()
        displayQuestion(((binding.questionText.tag as? Pair<*, *>)?.first as? Int ?: 0) + 1)
    }

    private fun showResult() {
        val normalizedTechniqueName = when (techniqueName) {
            "DiagonalReadingTechnique" -> "Чтение по диагонали"
            "KeywordSearchTechnique" -> "Поиск ключевых слов"
            "BlockReadingTechnique" -> "Чтение блоками"
            "SentenceReverseTechnique" -> "Предложения наоборот"
            "WordReverseTechnique" -> "Слова наоборот"
            "PointerMethodTechnique" -> "Метод указки"
            else -> techniqueName
        }

        val totalQuestions = when (normalizedTechniqueName) {
            "Чтение по диагонали" -> TextResources.diagonalTexts.getOrNull(currentTextIndex)?.questionsAndAnswers?.size
            "Поиск ключевых слов" -> TextResources.keywordTexts.getOrNull(currentTextIndex)?.questionsAndAnswers?.size
            else -> TextResources.otherTexts[normalizedTechniqueName]?.getOrNull(currentTextIndex)?.questionsAndAnswers?.size
        } ?: 0

        Log.d("TestFragment", "showResult: score=$score, totalQuestions=$totalQuestions, techniqueName='$normalizedTechniqueName'")

        binding.tvQuestionHeader.visibility = View.GONE
        binding.questionText.text = "Тест завершён! Ваш результат: $score из $totalQuestions"
        binding.radioGroup.visibility = View.GONE
        binding.btnSubmit.visibility = View.GONE

        saveTestResult(normalizedTechniqueName, totalQuestions)
    }

    private fun saveTestResult(normalizedTechniqueName: String, totalQuestions: Int) {
        val sharedPreferences = requireContext().getSharedPreferences("TestResults", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val key = "result_$normalizedTechniqueName" // Единый ключ для техники

        // Получаем существующий результат, если он есть
        val existingResultJson = sharedPreferences.getString(key, null)
        var shouldSave = true

        if (existingResultJson != null) {
            try {
                val existingResult = JSONObject(existingResultJson)
                val existingScore = existingResult.getInt("score")
                val existingDuration = existingResult.getLong("durationPerWord")

                // Сохраняем, если новый score выше, или если score равен, но durationPerWord меньше
                if (score < existingScore || (score == existingScore && durationPerWord >= existingDuration)) {
                    shouldSave = false
                }
            } catch (e: Exception) {
                Log.e("TestFragment", "Failed to parse existing result JSON: $existingResultJson", e)
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
            Log.d("TestFragment", "Saved result: key=$key, resultJson=$resultJson")
        } else {
            Log.d("TestFragment", "Skipped saving: new result (score=$score, duration=$durationPerWord) not better than existing")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}