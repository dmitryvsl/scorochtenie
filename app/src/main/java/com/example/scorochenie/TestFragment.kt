package com.example.scorochenie

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.scorochenie.databinding.FragmentTestBinding

class TestFragment : Fragment() {

    companion object {
        private const val ARG_TEXT_INDEX = "textIndex"
        private const val ARG_TECHNIQUE_NAME = "techniqueName"
        private const val ARG_DURATION_PER_WORD = "durationPerWord"

        fun newInstance(textIndex: Int, techniqueName: String, durationPerWord: Long): TestFragment {
            return TestFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_TEXT_INDEX, textIndex)
                    putString(ARG_TECHNIQUE_NAME, techniqueName)
                    putLong(ARG_DURATION_PER_WORD, durationPerWord)
                }
            }
        }
    }

    private var _binding: FragmentTestBinding? = null
    private val binding get() = _binding!!
    private var score = 0
    private var currentTextIndex = 0
    private var techniqueName: String = ""
    private var durationPerWord: Long = 400L
    private val questionsAndAnswers = mapOf(
        0 to listOf(
            "Как называется процесс, с помощью которого растения производят себе пищу?" to listOf("фотосинтез", "испарение", "брожение"),
            "Какие три вещества растения используют для питания?" to listOf("вода, углекислый газ, минералы", "кислород, азот, свет", "белки, жиры, углеводы"),
            "Кто был первым живым организмом, подготовившим почву для других растений?" to listOf("лишайники", "папоротники", "мхи"),
            "Где на Земле встречается наибольшее разнообразие растений?" to listOf("тропические леса", "арктические пустыни", "горные вершины"),
            "Что угрожает благополучию растений?" to listOf("деятельность человека", "изменения орбиты Земли", "движения тектонических плит")
        ),
        1 to listOf(
            "Что используется для преобразования звуковых вибраций в электрические сигналы?" to listOf("микрофон", "антенна", "динамик"),
            "Как называется процесс управления мощностью радиоволн в соответствии с вибрацией звука?" to listOf("манипулирование амплитудой", "электромагнитное торможение", "вибрационное кодирование"),
            "В чём измеряется частота радиоволн?" to listOf("в килогерцах и мегагерцах", "в метрах и литрах", "в паскалях и ньютонах"),
            "Почему приёмник может принимать только одну нужную станцию?" to listOf("потому что он настраивается на определённую частоту", "потому что звук изолируется микрофоном", "потому что приёмник имеет один динамик"),
            "Для чего используются радиоволны, помимо передачи звука?" to listOf("для связи, телевидения и управления", "только для освещения", "только для обогрева воздуха")
        ),
        2 to listOf(
            "Какой принцип используется в работе радара?" to listOf("отражение микроволн (эхо)", "испарение частиц", "гравитационное притяжение"),
            "Кто изобрёл первую радарную установку?" to listOf("Роберт Ватсон-Ватт", "Альберт Эйнштейн", "Никола Тесла"),
            "Для чего авиадиспетчеры используют радар?" to listOf("для определения положения и высоты самолёта", "для измерения температуры в салоне", "для подсчёта пассажиров"),
            "Как радар определяет расстояние до объекта?" to listOf("по времени возвращения отражённых волн", "по весу объекта", "по цвету изображения на дисплее"),
            "В каком году была создана первая радарная установка?" to listOf("в 1935 году", "в 1912 году", "в 1960 году")
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Получаем аргументы
        currentTextIndex = arguments?.getInt(ARG_TEXT_INDEX, 0) ?: 0
        techniqueName = arguments?.getString(ARG_TECHNIQUE_NAME) ?: ""
        durationPerWord = arguments?.getLong(ARG_DURATION_PER_WORD) ?: 400L

        displayQuestion(0)

        binding.btnSubmit.setOnClickListener {
            checkAnswer()
        }
    }

    private fun displayQuestion(index: Int) {
        if (index < (questionsAndAnswers[currentTextIndex]?.size ?: 0)) {
            val questionPair = questionsAndAnswers[currentTextIndex]?.get(index)
            binding.questionText.text = questionPair?.first ?: ""
            binding.questionText.tag = Pair(index, questionPair?.second?.get(0) ?: "") // Сохраняем индекс вопроса и правильный ответ

            // Устанавливаем заголовок с номером вопроса
            binding.tvQuestionHeader.text = "Вопрос ${index + 1}"

            // Очищаем и заполняем RadioGroup с перемешанными вариантами
            binding.radioGroup.removeAllViews()
            val options = questionPair?.second?.shuffled() ?: emptyList()
            options.forEach { option ->
                val radioButton = RadioButton(context).apply {
                    text = option
                    id = View.generateViewId()
                    textSize = 16f // Устанавливаем размер текста 16sp
                    setTextColor(ContextCompat.getColor(context, android.R.color.white)) // Цвет текста
                }
                binding.radioGroup.addView(radioButton)
            }
        } else {
            showResult()
        }
    }

    private fun checkAnswer() {
        val selectedRadioButtonId = binding.radioGroup.checkedRadioButtonId
        if (selectedRadioButtonId == -1) {
            // Показываем Toast, если ответ не выбран
            Toast.makeText(context, "Выберите ответ", Toast.LENGTH_SHORT).show()
            return // Прерываем выполнение, не переходя к следующему вопросу
        }

        // Обработка выбранного ответа
        val selectedRadioButton = binding.radioGroup.findViewById<RadioButton>(selectedRadioButtonId)
        val userAnswer = selectedRadioButton.text.toString().lowercase()
        val correctAnswer = (binding.questionText.tag as? Pair<*, *>)?.second as? String ?: ""
        if (userAnswer == correctAnswer.lowercase()) {
            score++
        }

        binding.radioGroup.clearCheck()
        displayQuestion(((binding.questionText.tag as? Pair<*, *>)?.first as? Int ?: 0) + 1)
    }

    private fun showResult() {
        binding.tvQuestionHeader.visibility = View.GONE // Скрываем заголовок
        binding.questionText.text = "Тест завершён! Ваш результат: $score из ${questionsAndAnswers[currentTextIndex]?.size ?: 0}"
        binding.radioGroup.visibility = View.GONE
        binding.btnSubmit.visibility = View.GONE

        // Сохраняем результат в SharedPreferences
        saveTestResult()
    }

    private fun saveTestResult() {
        val sharedPreferences = requireContext().getSharedPreferences("TestResults", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Уникальный ключ для записи: методика + временная метка
        val timestamp = System.currentTimeMillis()
        val key = "result_$techniqueName$timestamp"
        val resultJson = """
            {
                "techniqueName": "$techniqueName",
                "durationPerWord": $durationPerWord,
                "score": $score,
                "totalQuestions": ${questionsAndAnswers[currentTextIndex]?.size ?: 0},
                "timestamp": $timestamp
            }
        """
        editor.putString(key, resultJson)
        editor.apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}