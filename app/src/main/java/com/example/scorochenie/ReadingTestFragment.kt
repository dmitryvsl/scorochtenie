package com.example.scorochenie

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.example.scorochenie.databinding.FragmentReadingTestBinding
import kotlin.random.Random
import android.widget.TextView

class ReadingTestFragment : Fragment() {

    companion object {
        private const val ARG_TECHNIQUE_NAME = "technique_name"
        private const val ARG_DURATION_PER_WORD = "duration_per_word"
        fun newInstance(techniqueName: String, durationPerWord: Long): ReadingTestFragment {
            val fragment = ReadingTestFragment()
            val args = Bundle()
            args.putString(ARG_TECHNIQUE_NAME, techniqueName)
            args.putLong(ARG_DURATION_PER_WORD, durationPerWord)
            fragment.arguments = args
            return fragment
        }
    }

    private var _binding: FragmentReadingTestBinding? = null
    private val binding get() = _binding!!
    private lateinit var technique: ReadingTechnique
    private var durationPerWord: Long = 400L
    private var techniqueName: String = ""
    private var selectedTextIndex: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReadingTestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        techniqueName = arguments?.getString(ARG_TECHNIQUE_NAME) ?: ""
        durationPerWord = arguments?.getLong(ARG_DURATION_PER_WORD) ?: 400L

        // Нормализация имени техники
        val normalizedTechniqueName = when (techniqueName) {
            "DiagonalReadingTechnique" -> "Чтение по диагонали"
            "KeywordSearchTechnique" -> "Поиск ключевых слов"
            "BlockReadingTechnique" -> "Чтение блоками"
            "SentenceReverseTechnique" -> "Предложения наоборот"
            "WordReverseTechnique" -> "Слова наоборот"
            "PointerMethodTechnique" -> "Метод указки"
            else -> "Неизвестная техника"
        }

        // Инициализация техники
        technique = when (techniqueName) {
            "BlockReadingTechnique" -> BlockReadingTechnique()
            "DiagonalReadingTechnique" -> DiagonalReadingTechnique()
            "KeywordSearchTechnique" -> KeywordSearchTechnique()
            "PointerMethodTechnique" -> PointerMethodTechnique()
            "SentenceReverseTechnique" -> SentenceReverseTechnique()
            "WordReverseTechnique" -> WordReverseTechnique()
            else -> object : ReadingTechnique(normalizedTechniqueName) {
                override fun startAnimation(
                    textView: TextView,
                    guideView: View,
                    durationPerWord: Long,
                    selectedTextIndex: Int,
                    onAnimationEnd: () -> Unit
                ) {
                    textView.text = "Анимация недоступна"
                    guideView.visibility = View.INVISIBLE
                    onAnimationEnd()
                }
            }
        }

        // Выбираем размер списка текстов в зависимости от техники
        val textListSize = when (normalizedTechniqueName) {
            "Чтение по диагонали" -> TextResources.diagonalTexts.size
            "Поиск ключевых слов" -> TextResources.keywordTexts.size
            else -> TextResources.otherTexts[normalizedTechniqueName]?.size ?: 1
        }
        selectedTextIndex = Random.nextInt(textListSize)

        Log.d("ReadingTest", "Technique: $techniqueName, Normalized: $normalizedTechniqueName, Duration: $durationPerWord, TextIndex: $selectedTextIndex, TextListSize: $textListSize")

        if (normalizedTechniqueName == "Чтение по диагонали") {
            binding.scrollContainer.visibility = View.GONE
            binding.diagonalContainer.visibility = View.VISIBLE
            startReadingAnimation(binding.animationTextDiagonal)
        } else {
            binding.scrollContainer.visibility = View.VISIBLE
            binding.diagonalContainer.visibility = View.GONE
            startReadingAnimation(binding.animationTextScroll)
        }
    }

    private fun startReadingAnimation(textView: TextView) {
        val guideView = View(requireContext()).apply {
            visibility = View.INVISIBLE
            layoutParams = FrameLayout.LayoutParams(20, 2)
            setBackgroundColor(android.graphics.Color.BLACK)
        }

        val container = if (technique is DiagonalReadingTechnique) binding.diagonalContainer else binding.scrollContainer
        container.addView(guideView)

        technique.startAnimation(textView, guideView, durationPerWord, selectedTextIndex) {
            container.removeView(guideView)
            navigateToTest()
        }
    }

    private fun navigateToTest() {
        val fragment = TestFragment.newInstance(selectedTextIndex, techniqueName, durationPerWord)
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