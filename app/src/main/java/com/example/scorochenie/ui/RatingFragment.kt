package com.example.scorochenie.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scorochenie.data.TestResult
import com.example.scorochenie.databinding.FragmentRatingBinding
import org.json.JSONObject

class RatingFragment : Fragment() {

    private var _binding: FragmentRatingBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: RatingAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRatingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = RatingAdapter()
        binding.ratingRecyclerView.adapter = adapter
        binding.ratingRecyclerView.layoutManager = LinearLayoutManager(context)

        // Добавляем разделитель
        val dividerItemDecoration = DividerItemDecoration(
            binding.ratingRecyclerView.context,
            LinearLayoutManager.VERTICAL
        )
        binding.ratingRecyclerView.addItemDecoration(dividerItemDecoration)

        loadBestResults()
        binding.ratingHelpIcon.setOnClickListener {
            showHelpDialog("Здесь вы можете увидеть свои достижения в тренировке скорочтения! Рейтинг отражает ваш лучший результат по каждой технике:\n" +
                    "1. Просмотрите лучшие результаты: Узнайте, сколько на вопросов вы правильно ответили для каждой техники.\n" +
                    "2. Оцените скорость чтения: Посмотрите, с какой скоростью был достигнут ваш лучший результат.\n" +
                    "3. Стремитесь к прогрессу: Практикуйтесь, чтобы улучшить свои показатели!")
        }

    }
    private fun showHelpDialog(message: String) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Справка")
            .setMessage(message)
            .setPositiveButton("ОК", null)
            .show()
    }

    private fun loadBestResults() {
        val sharedPreferences = requireContext().getSharedPreferences("TestResults", Context.MODE_PRIVATE)
        val allEntries = sharedPreferences.all

        // Собираем все результаты
        val results = mutableListOf<TestResult>()
        for (entry in allEntries) {
            val jsonString = entry.value as? String ?: continue
            try {
                val json = JSONObject(jsonString)
                val result = TestResult(
                    techniqueName = json.getString("techniqueName"),
                    durationPerWord = json.getLong("durationPerWord"),
                    score = json.getInt("score"),
                    totalQuestions = json.getInt("totalQuestions"),
                    timestamp = json.optLong("timestamp", 0L) // Используем optLong для обратной совместимости
                )
                results.add(result)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Группируем по методике, выбираем лучший результат по score, сортируем по последнему timestamp
        val bestResults = results.groupBy { it.techniqueName }
            .mapValues { entry ->
                entry.value.maxByOrNull { it.score }!! // Выбираем результат с максимальным score
            }
            .values
            .sortedByDescending { techniqueResults ->
                // Находим максимальный timestamp для этой методики
                results.filter { it.techniqueName == techniqueResults.techniqueName }
                    .maxOfOrNull { it.timestamp } ?: 0L
            }

        adapter.setResults(bestResults)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}