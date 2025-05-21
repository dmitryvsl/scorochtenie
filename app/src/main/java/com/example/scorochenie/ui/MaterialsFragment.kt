package com.example.scorochenie.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.scorochenie.R
import com.example.scorochenie.domain.Technique
import android.widget.ImageView

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

        // Добавляем разделитель
        val dividerItemDecoration = DividerItemDecoration(
            recyclerView.context,
            LinearLayoutManager.VERTICAL
        )
        recyclerView.addItemDecoration(dividerItemDecoration)

        val techniques = listOf(
            Technique("Чтение по диагонали"),
            Technique("Поиск ключевых слов"),
            Technique("Чтение \"блоками\""),
            Technique("Предложения наоборот"),
            Technique("Слова наоборот"),
            Technique("Метод \"указки\"")
        )

        techniqueAdapter = TechniqueAdapter(techniques) { technique ->
            onTechniqueClicked(technique)
        }
        recyclerView.adapter = techniqueAdapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Настройка обработчика клика для иконки справки
        val helpIcon = view.findViewById<ImageView>(R.id.materials_help_icon)
        helpIcon.setOnClickListener {
            showHelpDialog("Этот раздел создан, чтобы вы могли глубже изучить техники скорочтения и выбрать те, которые вам подходят! Ознакомьтесь с материалами, чтобы освоить новые навыки:\n" +
                    "1. Просмотрите, как работают техники: Узнайте, как каждая техника помогает читать быстрее и лучше понимать текст.\n" +
                    "2. Прочитайте описания техник: Изучите подробные инструкции и примеры, чтобы применять техники на практике.\n" +
                    "3. Начните изучение: Погрузитесь в материалы и тренируйтесь, чтобы сделать чтение более эффективным и увлекательным!")
        }
    }

    private fun showHelpDialog(message: String) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Справка")
            .setMessage(message)
            .setPositiveButton("ОК", null)
            .show()
    }

    private fun onTechniqueClicked(technique: Technique) {
        val detailFragment = TechniqueDetailFragment.newInstance(technique.name)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, detailFragment)
            .addToBackStack(null) // Добавляет возможность вернуться назад
            .commit()
    }
}