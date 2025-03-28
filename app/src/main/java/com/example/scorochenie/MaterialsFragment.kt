package com.example.scorochenie

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MaterialsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var techniqueAdapter: TechniqueAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_materials, container, false)

        recyclerView = view.findViewById(R.id.materials_list)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val techniques = listOf(
            Technique("Чтение по диагонали"),
            Technique("Зигзагообразное чтение"),
            Technique("Вертикальное чтение"),
            Technique("Селективное чтение (поиск ключевых слов)"),
            Technique("Чтение \"блоками\""),
            Technique("Метрономное чтение"),
            Technique("Периферийное чтение"),
            Technique("Обратное чтение (регрессия)"),
            Technique("Метод \"указки\"")
        )

        techniqueAdapter = TechniqueAdapter(techniques) { technique ->
            onTechniqueClicked(technique)
        }
        recyclerView.adapter = techniqueAdapter

        return view
    }

    private fun onTechniqueClicked(technique: Technique) {
        // Здесь будет логика перехода к демонстрации техники
        // Пока просто выведем название техники в лог
        println("Выбрана техника: ${technique.name}")
    }
}