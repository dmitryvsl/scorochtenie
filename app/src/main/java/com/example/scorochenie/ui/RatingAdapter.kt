package com.example.scorochenie.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.scorochenie.domain.TestResult
import com.example.scorochenie.databinding.ItemRatingBinding
import com.example.scorochenie.domain.Technique
import com.example.scorochenie.domain.TechniqueType

class RatingAdapter : RecyclerView.Adapter<RatingAdapter.RatingViewHolder>() {

    private var results: List<TestResult> = emptyList()

    fun setResults(results: List<TestResult>) {
        this.results = results
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RatingViewHolder {
        val binding = ItemRatingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RatingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RatingViewHolder, position: Int) {
        holder.bind(results[position])
    }

    override fun getItemCount(): Int = results.size

    class RatingViewHolder(private val binding: ItemRatingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(result: TestResult) {
            binding.tvTechniqueName.text = TechniqueType.valueOf(result.techniqueName).displayName
            binding.tvSpeed.text = "Скорость: ${result.durationPerWord} слов/мин"
            binding.tvScore.text = "Результат: ${result.score} из ${result.totalQuestions}"
        }
    }
}