package com.example.scorochenie

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

    private lateinit var technique: ReadingTechnique
    private lateinit var guideView: View
    private var animationTextView: TextView? = null
    private var scrollView: ScrollView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_technique_detail, container, false)
        val techniqueName = arguments?.getString(ARG_TECHNIQUE_NAME)

        technique = when (techniqueName) {
            "Чтение по диагонали" -> DiagonalReadingTechnique()
            "Поиск ключевых слов" -> KeywordSearchTechnique()
            "Чтение \"блоками\"" -> BlockReadingTechnique()
            "Предложения наоборот" -> SentenceReverseTechnique()
            "Слова наоборот" -> WordReverseTechnique()
            "Метод \"указки\"" -> PointerMethodTechnique()
            else -> object : ReadingTechnique(techniqueName ?: "Неизвестная техника") {
                override fun startAnimation(
                    textView: TextView,
                    guideView: View,
                    durationPerWord: Long,
                    selectedTextIndex: Int,
                    onAnimationEnd: () -> Unit
                ) {
                    textView.text = "Анимация для этой техники недоступна"
                    guideView.visibility = View.INVISIBLE
                    onAnimationEnd()
                }
            }
        }

        val titleTextView = view.findViewById<TextView>(R.id.technique_title)
        val descriptionTextView = view.findViewById<TextView>(R.id.technique_description)
        val scrollContainer = view.findViewById<FrameLayout>(R.id.scroll_container)
        val diagonalContainer = view.findViewById<FrameLayout>(R.id.diagonal_container)
        val startButton = view.findViewById<Button>(R.id.start_button)
        val backButton = view.findViewById<Button>(R.id.back_button)

        titleTextView.text = technique.name
        descriptionTextView.text = technique.description

        guideView = View(requireContext()).apply {
            visibility = View.INVISIBLE
            layoutParams = FrameLayout.LayoutParams(20, 2).apply {
                setMargins(0, 0, 0, 0)
            }
            setBackgroundColor(android.graphics.Color.BLACK)
            Log.d("TechniqueDetail", "guideView initialized with visibility=$visibility")
        }

        // Выбираем нужный контейнер и TextView
        if (technique is DiagonalReadingTechnique) {
            diagonalContainer.visibility = View.VISIBLE
            scrollContainer.visibility = View.GONE
            animationTextView = view.findViewById(R.id.animation_text_diagonal)
        } else {
            scrollContainer.visibility = View.VISIBLE
            diagonalContainer.visibility = View.GONE
            animationTextView = view.findViewById(R.id.animation_text_scroll)
            scrollView = view.findViewById(R.id.scrollView)
        }

        if (technique is DiagonalReadingTechnique || technique is KeywordSearchTechnique || technique is BlockReadingTechnique ||
            technique is PointerMethodTechnique || technique is SentenceReverseTechnique || technique is WordReverseTechnique
        ) {
            animationTextView?.visibility = View.GONE
            startButton.visibility = View.VISIBLE
            startButton.setOnClickListener {
                descriptionTextView.visibility = View.GONE
                startButton.visibility = View.GONE
                animationTextView?.visibility = View.VISIBLE
                backButton.visibility = View.VISIBLE

                // Управление видимостью DiagonalLineView
                val diagonalLineView = diagonalContainer.findViewById<DiagonalLineView>(R.id.diagonal_line_view)
                if (technique is DiagonalReadingTechnique && diagonalLineView != null) {
                    diagonalLineView.visibility = View.VISIBLE
                    Log.d("TechniqueDetail", "DiagonalLineView set to VISIBLE")
                } else {
                    diagonalLineView?.visibility = View.GONE
                    Log.d("TechniqueDetail", "DiagonalLineView set to GONE")
                }

                // Добавляем guideView в активный контейнер
                val activeContainer = if (technique is DiagonalReadingTechnique) diagonalContainer else scrollContainer
                if (guideView.parent == null) {
                    activeContainer.addView(guideView)
                    Log.d("TechniqueDetail", "guideView added to activeContainer, visibility=${guideView.visibility}")
                }

                // Устанавливаем значение durationPerWord по умолчанию (400 WPM)
                val defaultDurationPerWord = 400L
                // Выбираем размер списка текстов в зависимости от техники
                val textListSize = when (technique) {
                    is DiagonalReadingTechnique -> TextResources.diagonalTexts.size
                    is KeywordSearchTechnique -> TextResources.keywordTexts.size
                    else -> TextResources.otherTexts[technique.name]?.size ?: 1
                }
                val selectedTextIndex = Random.nextInt(textListSize)

                Log.d("TechniqueDetail", "Starting animation with default durationPerWord=$defaultDurationPerWord WPM and textIndex=$selectedTextIndex, textListSize=$textListSize")

                animationTextView?.let { textView ->
                    technique.startAnimation(textView, guideView, defaultDurationPerWord, selectedTextIndex) {
                        val parent = guideView.parent as? ViewGroup
                        parent?.removeView(guideView)
                        animationTextView?.visibility = View.VISIBLE
                        backButton.visibility = View.VISIBLE
                        guideView.visibility = View.INVISIBLE
                        Log.d("TechniqueDetail", "Animation ended, guideView removed and set to INVISIBLE")
                    }
                }
            }
        } else {
            animationTextView?.text = "Анимация для этой техники в разработке."
            animationTextView?.visibility = View.VISIBLE
            startButton.visibility = View.GONE
            val diagonalLineView = diagonalContainer.findViewById<DiagonalLineView>(R.id.diagonal_line_view)
            diagonalLineView?.visibility = View.GONE
            guideView.visibility = View.INVISIBLE
            Log.d("TechniqueDetail", "No animation, guideView set to INVISIBLE")
        }

        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Очистка guideView
        val parent = guideView.parent as? ViewGroup
        parent?.removeView(guideView)
        guideView.visibility = View.INVISIBLE
        Log.d("TechniqueDetail", "onDestroyView: guideView removed and set to INVISIBLE")
        animationTextView = null
        scrollView = null
    }
}