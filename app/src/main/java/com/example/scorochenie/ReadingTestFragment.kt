package com.example.scorochenie

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.example.scorochenie.databinding.FragmentReadingTestBinding

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReadingTestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val techniqueName = arguments?.getString(ARG_TECHNIQUE_NAME) ?: ""
        durationPerWord = arguments?.getLong(ARG_DURATION_PER_WORD) ?: 400L

        // Определяем, какую технику используем
        technique = when (techniqueName) {
            "BlockReadingTechnique" -> BlockReadingTechnique()
            "DiagonalReadingTechnique" -> DiagonalReadingTechnique()
            "KeywordSearchTechnique" -> KeywordSearchTechnique()
            "PointerMethodTechnique" -> PointerMethodTechnique()
            "SentenceReverseTechnique" -> SentenceReverseTechnique()
            "WordReverseTechnique" -> WordReverseTechnique()
            else -> object : ReadingTechnique("Неизвестная техника") {
                override fun startAnimation(textView: android.widget.TextView, guideView: View, onAnimationEnd: () -> Unit) {
                    textView.text = "Анимация недоступна"
                    guideView.visibility = View.INVISIBLE
                    onAnimationEnd()
                }
            }
        }

        // Переключаем видимость контейнеров в зависимости от техники
        if (techniqueName == "DiagonalReadingTechnique") {
            binding.scrollContainer.visibility = View.GONE
            binding.diagonalContainer.visibility = View.VISIBLE
            startReadingAnimation(binding.animationTextDiagonal)
        } else {
            binding.scrollContainer.visibility = View.VISIBLE
            binding.diagonalContainer.visibility = View.GONE
            startReadingAnimation(binding.animationTextScroll)
        }
    }

    private fun startReadingAnimation(textView: android.widget.TextView) {
        val guideView = View(requireContext()).apply {
            visibility = View.INVISIBLE // Устанавливаем невидимость при создании
            layoutParams = FrameLayout.LayoutParams(20, 2)
            setBackgroundColor(android.graphics.Color.BLACK)
            Log.d("ReadingTest", "guideView created with visibility=$visibility")
        }

        // Добавляем guideView в соответствующий контейнер
        if (technique is DiagonalReadingTechnique) {
            binding.diagonalContainer.addView(guideView)
            Log.d("ReadingTest", "guideView added to diagonalContainer, visibility=${guideView.visibility}")
        } else {
            binding.scrollContainer.addView(guideView)
            Log.d("ReadingTest", "guideView added to scrollContainer, visibility=${guideView.visibility}")
        }

        technique.startAnimation(textView, guideView) {
            // Удаляем guideView из соответствующего контейнера
            if (technique is DiagonalReadingTechnique) {
                binding.diagonalContainer.removeView(guideView)
                Log.d("ReadingTest", "guideView removed from diagonalContainer, visibility=${guideView.visibility}")
            } else {
                binding.scrollContainer.removeView(guideView)
                Log.d("ReadingTest", "guideView removed from scrollContainer, visibility=${guideView.visibility}")
            }
            navigateToTest()
        }
    }

    private fun navigateToTest() {
        val fragment = TestFragment.newInstance()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d("ReadingTest", "onDestroyView called")
    }
}