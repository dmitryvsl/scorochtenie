package com.example.scorochenie.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.scorochenie.R
import com.example.scorochenie.domain.TextResources
import com.example.scorochenie.domain.Technique
import com.example.scorochenie.domain.TechniqueType
import kotlin.random.Random

class TechniqueDetailFragment : Fragment() {

    companion object {
        private const val ARG_TECHNIQUE_TYPE = "technique_type"

        fun newInstance(techniqueType: TechniqueType): TechniqueDetailFragment {
            val fragment = TechniqueDetailFragment()
            val args = Bundle()
            args.putSerializable(ARG_TECHNIQUE_TYPE, techniqueType)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var technique: Technique
    private lateinit var guideView: View
    private var animationTextView: TextView? = null
    private var scrollView: ScrollView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_technique_detail, container, false)

        val techniqueType = arguments?.getSerializable(ARG_TECHNIQUE_TYPE).let {
            it as? TechniqueType
                ?: throw RuntimeException("Can't find argument '${ARG_TECHNIQUE_TYPE}' in fragment 'TechniqueDetailFragment'")
        }

        technique = Technique.createTechnique(techniqueType)

        val titleTextView = view.findViewById<TextView>(R.id.technique_title)
        val descriptionTextView = view.findViewById<TextView>(R.id.technique_description)
        val scrollContainer = view.findViewById<FrameLayout>(R.id.scroll_container)
        val diagonalContainer = view.findViewById<FrameLayout>(R.id.diagonal_container)
        val startButton = view.findViewById<Button>(R.id.start_button)
        val backButton = view.findViewById<Button>(R.id.back_button)

        titleTextView.text = techniqueType.displayName
        descriptionTextView.text = technique.description

        guideView = View(requireContext()).apply {
            visibility = View.INVISIBLE
            layoutParams = FrameLayout.LayoutParams(20, 2).apply {
                setMargins(0, 0, 0, 0)
            }
            setBackgroundColor(android.graphics.Color.BLACK)
        }

        val isDiagonalTechnique = techniqueType == TechniqueType.DiagonalReading
        diagonalContainer.visibility = if (isDiagonalTechnique) View.VISIBLE else View.GONE
        scrollContainer.visibility = if (isDiagonalTechnique) View.GONE else View.VISIBLE
        animationTextView =
            view.findViewById(if (isDiagonalTechnique) R.id.animation_text_diagonal else R.id.animation_text_scroll)
        scrollView = if (isDiagonalTechnique) null else view.findViewById(R.id.scrollView)

        val isAnimationSupported =
            technique.description.toString() != "Описание для этой техники недоступно"
        if (isAnimationSupported) {
            animationTextView?.visibility = View.GONE
            startButton.visibility = View.VISIBLE
            startButton.setOnClickListener {
                descriptionTextView.visibility = View.GONE
                startButton.visibility = View.GONE
                animationTextView?.visibility = View.VISIBLE
                backButton.visibility = View.VISIBLE

                val diagonalLineView = diagonalContainer.findViewById<View>(R.id.diagonal_line_view)
                if (isDiagonalTechnique && diagonalLineView != null) {
                    diagonalLineView.visibility = View.VISIBLE
                } else {
                    diagonalLineView?.visibility = View.GONE
                }
                val activeContainer =
                    if (isDiagonalTechnique) diagonalContainer else scrollContainer
                if (guideView.parent == null) {
                    activeContainer.addView(guideView)
                }

                val defaultDurationPerWord = 200L
                val textListSize = when (techniqueType.displayName) {
                    "Чтение по диагонали" -> TextResources.getDiagonalTexts().size
                    "Поиск ключевых слов" -> TextResources.getKeywordTexts().size
                    else -> TextResources.getOtherTexts()[techniqueType.displayName]?.size ?: 1
                }
                val selectedTextIndex = Random.nextInt(textListSize)


                animationTextView?.let { textView ->
                    technique.startAnimation(
                        textView,
                        guideView,
                        defaultDurationPerWord,
                        selectedTextIndex
                    ) {
                        val parent = guideView.parent as? ViewGroup
                        parent?.removeView(guideView)
                        animationTextView?.visibility = View.VISIBLE
                        backButton.visibility = View.VISIBLE
                        guideView.visibility = View.INVISIBLE
                    }
                }
            }
        } else {
            animationTextView?.text = "Анимация для этой техники в разработке."
            animationTextView?.visibility = View.VISIBLE
            startButton.visibility = View.GONE
            val diagonalLineView = diagonalContainer.findViewById<View>(R.id.diagonal_line_view)
            diagonalLineView?.visibility = View.GONE
            guideView.visibility = View.INVISIBLE
        }

        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val parent = guideView.parent as? ViewGroup
        parent?.removeView(guideView)
        guideView.visibility = View.INVISIBLE
        animationTextView = null
        scrollView = null
    }
}