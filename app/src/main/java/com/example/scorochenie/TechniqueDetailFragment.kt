package com.example.scorochenie

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Button
import androidx.fragment.app.Fragment

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_technique_detail, container, false)

        val techniqueName = arguments?.getString(ARG_TECHNIQUE_NAME)
        technique = when (techniqueName) {
            "Чтение по диагонали" -> DiagonalReadingTechnique()
            "Вертикальное чтение" -> VerticalReadingTechnique()
            "Поиск ключевых слов" -> KeywordSearchTechnique()
            "Чтение \"блоками\"" -> BlockReadingTechnique()
            "Периферийное чтение" -> PeripheralReadingTechnique()
            "Обратное чтение" -> ReverseReadingTechnique()
            "Метод \"указки\"" -> PointerMethodTechnique()
            else -> object : ReadingTechnique(techniqueName ?: "Неизвестная техника") {
                override fun startAnimation(
                    textView: TextView,
                    guideView: View,
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
        val animationTextView = view.findViewById<TextView>(R.id.animation_text)
        val startButton = view.findViewById<Button>(R.id.start_button)
        val backButton = view.findViewById<Button>(R.id.back_button)

        titleTextView.text = technique.name
        descriptionTextView.text = technique.description

        guideView = View(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(2, animationTextView.height)
            setBackgroundColor(Color.RED)
            rotation = if (technique is PointerMethodTechnique) 0f else 45f // Без наклона для метода указки
            visibility = View.INVISIBLE
        }

        if (technique is DiagonalReadingTechnique || technique is KeywordSearchTechnique || technique is ReverseReadingTechnique || technique is PointerMethodTechnique) {
            animationTextView.visibility = View.GONE
            startButton.visibility = View.VISIBLE
            startButton.setOnClickListener {
                descriptionTextView.visibility = View.GONE
                startButton.visibility = View.GONE
                animationTextView.visibility = View.VISIBLE
                (view as ViewGroup).addView(guideView)

                technique.startAnimation(
                    animationTextView,
                    guideView
                ) {
                    // Callback при завершении анимации
                    guideView.visibility = View.INVISIBLE
                }
            }
        } else {
            animationTextView.text = "Анимация для этой техники в разработке."
            startButton.visibility = View.GONE
        }

        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Очистка ресурсов, если нужно
    }
}