package com.example.scorochenie

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ExercisesFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_exercises, container, false)

        // Настройка RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.exercises_list)
        recyclerView.layoutManager = LinearLayoutManager(context)
        // Добавляем разделитель
        val dividerItemDecoration = DividerItemDecoration(
            recyclerView.context,
            LinearLayoutManager.VERTICAL
        )
        recyclerView.addItemDecoration(dividerItemDecoration)
        // Список техник
        val techniques = listOf(
            TechniqueItem("DiagonalReadingTechnique", "Чтение по диагонали"),
            TechniqueItem("KeywordSearchTechnique", "Поиск ключевых слов"),
            TechniqueItem("BlockReadingTechnique", "Чтение \"блоками\""),
            TechniqueItem("SentenceReverseTechnique", "Предложения наоборот"),
            TechniqueItem("WordReverseTechnique", "Слова наоборот"),
            TechniqueItem("PointerMethodTechnique", "Метод \"указки\"")
        )

        // Установка адаптера
        recyclerView.adapter = TechniqueSelectionAdapter(techniques) { techniqueName ->
            navigateToSpeedSelection(techniqueName)
        }

        return view
    }

    private fun navigateToSpeedSelection(techniqueName: String) {
        val fragment = SpeedSelectionFragment.newInstance(techniqueName)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}

// Временная data class для хранения информации о технике
data class TechniqueItem(val name: String, val displayName: String)