package com.example.scorochenie.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.scorochenie.R
import com.example.scorochenie.databinding.FragmentReadingTestBinding
import com.example.scorochenie.domain.DiagonalReadingTechnique
import com.example.scorochenie.domain.TextResources
import com.example.scorochenie.domain.Technique
import com.example.scorochenie.domain.TechniqueType
import kotlin.random.Random

class ReadingTestFragment : Fragment() {

    companion object {

        private const val ARG_TECHNIQUE_TYPE = "technique_type"
        private const val ARG_DURATION_PER_WORD = "duration_per_word"

        fun newInstance(techniqueType: TechniqueType, durationPerWord: Long): ReadingTestFragment {
            val fragment = ReadingTestFragment()
            val args = Bundle()
            args.putSerializable(ARG_TECHNIQUE_TYPE, techniqueType)
            args.putLong(ARG_DURATION_PER_WORD, durationPerWord)
            fragment.arguments = args
            return fragment
        }
    }

    private var _binding: FragmentReadingTestBinding? = null
    private val binding get() = _binding!!

    private lateinit var technique: Technique
    private lateinit var techniqueType: TechniqueType
    private var durationPerWord: Long = 400L
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

        parseArguments()

        technique = Technique.createTechnique(techniqueType)

        val textListSize = when (techniqueType) {
            TechniqueType.DiagonalReading -> TextResources.getDiagonalTexts().size
            TechniqueType.KeywordSearch -> TextResources.getKeywordTexts().size
            else -> TextResources.getOtherTexts()[techniqueType.displayName]?.size ?: 1 //TODO отрефакторить TextResources на использование класса TechniqueType
        }
        selectedTextIndex = Random.nextInt(textListSize)

        if (technique is DiagonalReadingTechnique) {
            binding.scrollContainer.visibility = View.GONE
            binding.diagonalContainer.visibility = View.VISIBLE
            startReadingAnimation(binding.animationTextDiagonal)
        } else {
            binding.scrollContainer.visibility = View.VISIBLE
            binding.diagonalContainer.visibility = View.GONE
            startReadingAnimation(binding.animationTextScroll)
        }
    }

    private fun parseArguments(){
        techniqueType = arguments?.getSerializable(ARG_TECHNIQUE_TYPE).let {
            it as? TechniqueType ?: throw RuntimeException("Can't find argument '${ARG_TECHNIQUE_TYPE}' in fragment 'ReadingTestFragment'")
        }

        durationPerWord = arguments?.getLong(ARG_DURATION_PER_WORD)
            ?: throw RuntimeException("Can't find argument '${ARG_DURATION_PER_WORD}' in fragment 'ReadingTestFragment'")

    }

    private fun startReadingAnimation(textView: TextView) {
        val guideView = View(requireContext()).apply {
            visibility = View.INVISIBLE
            layoutParams = FrameLayout.LayoutParams(20, 2)
            setBackgroundColor(android.graphics.Color.BLACK)
        }

        val container =
            if (technique is DiagonalReadingTechnique) binding.diagonalContainer else binding.scrollContainer
        container.addView(guideView)

        technique.startAnimation(textView, guideView, durationPerWord, selectedTextIndex) {
            if (isAdded && !isDetached && !isRemoving) {
                container.removeView(guideView)
                navigateToTest()
            }
        }
    }

    private fun navigateToTest() {
        if (isAdded && !isDetached && !isRemoving) {
            val fragment =
                TestFragment.newInstance(selectedTextIndex, techniqueType, durationPerWord)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        technique.cancelAnimation()
        _binding = null
    }
}