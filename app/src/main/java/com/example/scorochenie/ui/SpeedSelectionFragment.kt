package com.example.scorochenie.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.scorochenie.R
import com.example.scorochenie.databinding.FragmentSpeedSelectionBinding

class SpeedSelectionFragment : Fragment() {

    companion object {
        private const val ARG_TECHNIQUE_NAME = "technique_name"

        fun newInstance(techniqueName: String): SpeedSelectionFragment {
            return SpeedSelectionFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TECHNIQUE_NAME, techniqueName)
                }
            }
        }
    }

    private var _binding: FragmentSpeedSelectionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSpeedSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Получаем techniqueName из аргументов
        val techniqueName = arguments?.getString(ARG_TECHNIQUE_NAME) ?: ""

        val techniqueDisplayName = when (techniqueName) {
            "BlockReadingTechnique" -> "Чтение \"блоками\""
            "DiagonalReadingTechnique" -> "Чтение по диагонали"
            "KeywordSearchTechnique" -> "Поиск ключевых слов"
            "PointerMethodTechnique" -> "Метод \"указки\""
            "SentenceReverseTechnique" -> "Предложения наоборот"
            "WordReverseTechnique" -> "Слова наоборот"
            else -> techniqueName
        }
        binding.tvTechniqueTitle.text = techniqueDisplayName

        // Обработчики кнопок скорости
        binding.btnSlowSpeed.setOnClickListener {
            navigateToReadingTest(techniqueName, 200L)
        }
        binding.btnMediumSpeed.setOnClickListener {
            navigateToReadingTest(techniqueName, 400L)
        }
        binding.btnFastSpeed.setOnClickListener {
            navigateToReadingTest(techniqueName, 600L)
        }
    }

    private fun navigateToReadingTest(techniqueName: String, durationPerWord: Long) {
        val fragment = ReadingTestFragment.newInstance(techniqueName, durationPerWord)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}