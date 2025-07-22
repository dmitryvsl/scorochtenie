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
import android.widget.ImageView
import com.example.scorochenie.domain.Technique
import com.example.scorochenie.domain.TechniqueType

class ExercisesFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_exercises, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.exercises_list)
        recyclerView.layoutManager = LinearLayoutManager(context)
        val dividerItemDecoration = DividerItemDecoration(
            recyclerView.context,
            LinearLayoutManager.VERTICAL
        )
        recyclerView.addItemDecoration(dividerItemDecoration)

        val techniques = Technique.getAllTechniques()

        recyclerView.adapter = TechniqueSelectionAdapter(techniques) { technique ->
            navigateToSpeedSelection(technique)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val helpIcon = view.findViewById<ImageView>(R.id.exercises_help_icon)
        helpIcon.setOnClickListener {
            showHelpDialog("Здесь вы найдете список упражнений для тренировки скорочтения. Погрузитесь в процесс и развивайте свои навыки чтения:\n" +
                    "1. Выберите технику: Определите, какую технику скорочтения вы хотите практиковать.\n" +
                    "2. Настройте скорость: Установите комфортный темп чтения для эффективной тренировки.\n" +
                    "3. Прочитайте текст: Внимательно ознакомьтесь с предложенным текстом.\n" +
                    "4. Ответьте на вопросы: Проверьте понимание материала, ответив на вопросы по тексту.")
        }
    }

    private fun showHelpDialog(message: String) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Справка")
            .setMessage(message)
            .setPositiveButton("ОК", null)
            .show()
    }

    private fun navigateToSpeedSelection(techniqueType: TechniqueType) {
        val fragment = SpeedSelectionFragment.newInstance(techniqueType)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}