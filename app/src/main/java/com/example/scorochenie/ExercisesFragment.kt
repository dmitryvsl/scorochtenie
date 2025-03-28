package com.example.scorochenie

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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
        // Здесь нужно добавить адаптер для списка упражнений
        // recyclerView.adapter = ExercisesAdapter(listOf("Упражнение 1", "Упражнение 2", ...))

        return view
    }
}