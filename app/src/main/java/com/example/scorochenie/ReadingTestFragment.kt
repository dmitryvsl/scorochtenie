package com.example.scorochenie

import android.os.Bundle
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

        // Запускаем анимацию сразу после создания представления
        startReadingAnimation()
    }

    private fun startReadingAnimation() {
        val textView = binding.animationText
        val guideView = View(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(20, 2)
            setBackgroundColor(android.graphics.Color.BLACK)
        }
        binding.container.addView(guideView)

        technique.startAnimation(textView, guideView) {
            binding.container.removeView(guideView)
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
    }
}