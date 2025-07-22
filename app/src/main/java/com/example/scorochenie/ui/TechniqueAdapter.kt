package com.example.scorochenie.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.scorochenie.domain.Technique
import com.example.scorochenie.domain.TechniqueType

class TechniqueAdapter(
    private val techniques: List<TechniqueType>,
    private val onItemClick: (TechniqueType) -> Unit // Callback для обработки кликов
) : RecyclerView.Adapter<TechniqueAdapter.TechniqueViewHolder>() {

    class TechniqueViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val techniqueName: TextView = itemView.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TechniqueViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return TechniqueViewHolder(view)
    }

    override fun onBindViewHolder(holder: TechniqueViewHolder, position: Int) {
        val technique = techniques[position]
        holder.techniqueName.text = technique.displayName
        holder.itemView.setOnClickListener {
            onItemClick(technique)
        }
    }

    override fun getItemCount(): Int = techniques.size
}
