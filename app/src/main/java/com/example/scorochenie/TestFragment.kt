package com.example.scorochenie

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import com.example.scorochenie.databinding.FragmentTestBinding

class TestFragment : Fragment() {

    companion object {
        fun newInstance(): TestFragment {
            return TestFragment()
        }
    }

    private var _binding: FragmentTestBinding? = null
    private val binding get() = _binding!!
    private var score = 0
    private var currentTextIndex = 0
    private val questionsAndAnswers = mapOf(
        0 to listOf(
            "Какой процесс используют растения в тексте для производства пищи?" to listOf("фотосинтез", "дыхание", "рост"),
            "Какой газ необходим растениям для фотосинтеза?" to listOf("углекислый газ", "кислород", "азот")
        ),
        1 to listOf(
            "Что передаёт радиоволны в тексте?" to listOf("передатчик", "приёмник", "антенна"),
            "Кто изобрёл первый радар?" to listOf("Роберт Ватсон-Ватт", "Никола Тесла", "Густав Гильом")
        ),
        2 to listOf(
            "Какой элемент был открыт в тексте?" to listOf("гелий", "водород", "кислород"),
            "Кто открыл этот элемент?" to listOf("Пьер Жансен", "Антуан Лавуазье", "Михаил Ломоносов")
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTestBinding.inflate(inflater, container, false)
        val view = binding.root

        // Предполагаем, что currentTextIndex передаётся из предыдущего фрагмента
        currentTextIndex = arguments?.getInt("textIndex", 0) ?: 0
        displayQuestion(0)

        binding.btnSubmit.setOnClickListener {
            checkAnswer()
        }

        return view
    }

    private fun displayQuestion(index: Int) {
        if (index < (questionsAndAnswers[currentTextIndex]?.size ?: 0)) {
            binding.questionText.text = questionsAndAnswers[currentTextIndex]?.get(index)?.first ?: ""
            binding.questionText.tag = index

            // Очищаем и заполняем RadioGroup
            binding.radioGroup.removeAllViews()
            val options = questionsAndAnswers[currentTextIndex]?.get(index)?.second ?: emptyList()
            options.forEach { option ->
                val radioButton = RadioButton(context).apply {
                    text = option
                    id = View.generateViewId()
                }
                binding.radioGroup.addView(radioButton)
            }
        } else {
            showResult()
        }
    }

    private fun checkAnswer() {
        val selectedRadioButtonId = binding.radioGroup.checkedRadioButtonId
        if (selectedRadioButtonId != -1) {
            val selectedRadioButton = binding.radioGroup.findViewById<RadioButton>(selectedRadioButtonId)
            val userAnswer = selectedRadioButton.text.toString().lowercase()
            val correctAnswer = questionsAndAnswers[currentTextIndex]?.get(binding.questionText.tag as? Int ?: 0)?.second?.get(0)?.lowercase()
            if (userAnswer == correctAnswer) {
                score++
            }
        }
        binding.radioGroup.clearCheck()
        displayQuestion((binding.questionText.tag as? Int ?: 0) + 1)
    }

    private fun showResult() {
        binding.questionText.text = "Тест завершён! Ваш результат: $score из ${questionsAndAnswers[currentTextIndex]?.size ?: 0}"
        binding.radioGroup.visibility = View.GONE
        binding.btnSubmit.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}