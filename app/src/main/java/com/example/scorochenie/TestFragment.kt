package com.example.scorochenie

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private val questions = listOf(
        "Какой процесс используют растения для производства пищи?" to "фотосинтез",
        "Что передаёт радиоволны в радио?" to "передатчик",
        "Какой учёный изобрёл первый радар?" to "Роберт Ватсон-Ватт"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTestBinding.inflate(inflater, container, false)
        val view = binding.root

        displayQuestion(0)

        binding.btnSubmit.setOnClickListener {
            checkAnswer()
        }

        return view
    }

    private fun displayQuestion(index: Int) {
        if (index < questions.size) {
            val (question, _) = questions[index]
            binding.questionText.text = question
            binding.questionText.tag = index
            binding.answerEditText.text.clear()
        } else {
            showResult()
        }
    }

    private fun checkAnswer() {
        val userAnswer = binding.answerEditText.text.toString().trim().lowercase()
        val correctAnswer = questions.getOrNull(binding.questionText.tag as? Int ?: 0)?.second?.lowercase()
        if (userAnswer == correctAnswer) {
            score++
        }
        displayQuestion((binding.questionText.tag as? Int ?: 0) + 1)
    }

    private fun showResult() {
        binding.questionText.text = "Тест завершён! Ваш результат: $score из ${questions.size}"
        binding.answerEditText.visibility = View.GONE
        binding.btnSubmit.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}