package com.example.scorochenie.ui

import android.os.Bundle
import android.util.Log
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
import kotlin.random.Random

class TechniqueDetailFragment : Fragment() {

    companion object {
        private const val ARG_TECHNIQUE_NAME = "technique_name"
        fun newInstance(techniqueName: String): TechniqueDetailFragment {
            val fragment = TechniqueDetailFragment()
            val args = Bundle()
            args.putString(ARG_TECHNIQUE_NAME, techniqueName)
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
        val techniqueName = arguments?.getString(ARG_TECHNIQUE_NAME) ?: ""

        // Используем Technique.createTechnique для создания техники
        technique = Technique.createTechnique(techniqueName)

        val titleTextView = view.findViewById<TextView>(R.id.technique_title)
        val descriptionTextView = view.findViewById<TextView>(R.id.technique_description)
        val scrollContainer = view.findViewById<FrameLayout>(R.id.scroll_container)
        val diagonalContainer = view.findViewById<FrameLayout>(R.id.diagonal_container)
        val startButton = view.findViewById<Button>(R.id.start_button)
        val backButton = view.findViewById<Button>(R.id.back_button)

        titleTextView.text = technique.displayName
        descriptionTextView.text = technique.description

        guideView = View(requireContext()).apply {
            visibility = View.INVISIBLE
            layoutParams = FrameLayout.LayoutParams(20, 2).apply {
                setMargins(0, 0, 0, 0)
            }
            setBackgroundColor(android.graphics.Color.BLACK)
        }

        // Проверяем, является ли техника DiagonalReadingTechnique
        val isDiagonalTechnique = technique.displayName == "Чтение по диагонали"
        diagonalContainer.visibility = if (isDiagonalTechnique) View.VISIBLE else View.GONE
        scrollContainer.visibility = if (isDiagonalTechnique) View.GONE else View.VISIBLE
        animationTextView = view.findViewById(if (isDiagonalTechnique) R.id.animation_text_diagonal else R.id.animation_text_scroll)
        scrollView = if (isDiagonalTechnique) null else view.findViewById(R.id.scrollView)

        // Проверяем, поддерживает ли техника анимацию
        val isAnimationSupported = technique.description.toString() != "Описание для этой техники недоступно"
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
                    Log.d("TechniqueDetail", "DiagonalLineView set to VISIBLE")
                } else {
                    diagonalLineView?.visibility = View.GONE
                }
                val activeContainer = if (isDiagonalTechnique) diagonalContainer else scrollContainer
                if (guideView.parent == null) {
                    activeContainer.addView(guideView)
                }

                val defaultDurationPerWord = 200L
                val textListSize = when (technique.displayName) {
                    "Чтение по диагонали" -> TextResources.getDiagonalTexts().size
                    "Поиск ключевых слов" -> TextResources.getKeywordTexts().size
                    else -> TextResources.getOtherTexts()[technique.displayName]?.size ?: 1
                }
                val selectedTextIndex = Random.nextInt(textListSize)


                animationTextView?.let { textView ->
                    technique.startAnimation(textView, guideView, defaultDurationPerWord, selectedTextIndex) {
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