package com.example.scorochenie

import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import kotlin.math.abs
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

    private val sampleTexts = listOf(
        // ... (твой текст остаётся без изменений) ...
        """
        Зелёные растения – единственный вид живых существ, способный самостоятельно производить свою пищу. Растение получают пищу из воды, углекислого газа и минералов почвы, также они используют энергию Солнца. Этот процесс называется фотосинтезом. В результате также выделяется кислород, необходимый для всех живых существ. Животные пользуются пищей, созданной растениями, поедая растения или тех животных, которые питаются этими растениями. Без растений все животные и люди должны были бы умереть. Первые растения представляли собой отдельные клетки, плававшие в океане, покрывавшем всю землю. Постепенно некоторые клетки образовали соединения, каждое с особой задачей. Корень – чтобы удерживать растение на месте; стебель – чтобы обеспечить процесс фотосинтеза и размножение. Водоросли остались простейшими. Когда растения выбрались на землю, им пришлось приспособиться к жизни на суше без поддержки воды. Стебли растений становились толще, и у них развился корень. Первые наземные растения обитали лишь в сырых местах, однако нынешние растения распространены в разных местах: от границ арктических льдов до тропических джунглей. Первыми растениями, распространёнными на новой территории, были лишайники. Они нарастают, словно кора, на обнажённой поверхности скал. Умирая, они сгнивают и превращаются в первый слой почвы. На этом слое почвы вырастают мхи, и их смерть и разложение, в свою очередь, формирует второй слой почвы. Вскоре почвы становится достаточно, чтобы росли папоротники и цветковые растения. Таким образом развивается сообщество растений, обеспечивая пищей животных и образуя единую среду обитания. Ботаникам известно около 380 000 видов растений. Большинство из них можно найти в тропических лесах. Из 250 000 видов цветковых растений 90 000 принадлежат к флоре Центральной и Южной Америки, а ещё 30 000 – к флоре тропической Африки. Далее на север разнообразие растений уменьшается. В Великобритании насчитывается всего 1800 видов. Растения закрепляются практически везде на нашей планете. Однако их благополучию угрожают люди в своих поисках пищи, топлива и пространства для жизни.
        """.trimIndent(),
        """
        Если ты включишь радио, то услышишь человека, который говорит в сотнях и тысячах миль от тебя. Звук не распространяется так далеко. Чтобы направлять поток волн, используется передатчик. Радиоприёмник улавливает радиоволны и использует их для того, чтобы передать копию первоначального звука. Когда человек говорит в микрофон, его голос воспроизводит вибрацию воздуха. Микрофон превращает вибрацию в слабый переменный электрический ток, то есть в электрические сигналы. Электрические сигналы поступают на передатчик, который превращает их в радиоволны, заставляя ток резко подниматься и опускаться по антенне. Радиоволны выходят из передатчика в виде единого потока, это несущая волна. В простейших типах передатчиков сигналы из микрофона контролируют мощность испускаемых радиоволн. Это значит, что радиоволны пульсируют, меняя свою мощность, в соответствии со звуковой вибрацией. Такое управление радиоволнами называется манипулирование амплитудой. Пульсирующие радиоволны передатчика, радиосигналы, улавливаются антенной приёмника. Приёмник превращает волны вновь в электросигналы, которые поступают вновь в громкоговоритель. Громкоговоритель воспроизводит в воздухе точно такие же вибрации, как те, которые поступали в микрофон, так что мы слышим копию первоначального звука. Передатчики каждую секунду передают миллионы разных волн. Количество волн в секунду называется частотой. Она отмечена на шкале настройки радиоприёмника либо в килогерцах (тысяча волн в секунду), либо в мегагерцах (в миллионах волн в секунду). Различные станции используют различные частоты, поэтому, чтобы выбрать ту, которая тебе нужна, надо настроить приёмник. Радиоволны используются во многих других средствах коммуникации, кроме просто передачи звука. Полиция, пожарные, таксисты и врачи «скорой помощи» используют двухстороннее радио, чтобы переговариваться со штабом и друг с другом. Радиотелефон подключается к телефону сети через радио. Корабли и самолёты тоже используют радио для переговоров и ориентации, поскольку по сигналу радиомаяка они определяют своё местонахождение. Телевидение применяет радиоволны для передачи звука и изображения. По радио можно управлять большими космическими кораблями, моделями автомашин, лодками и самолётами. Некоторые радиоволны проходят тысячи километров вокруг земли, двигаясь между ионосферой и поверхностью земли. Они мчатся со скоростью 290000 (двести девяносто тысяч) километров в секунду. Мы не успеем и глазом моргнуть, как радиоволны совершат кругосветное путешествие.
        """.trimIndent(),
        """
        Радар позволяет нам обнаружить положение неподвижного объекта, даже если он находится очень далеко или в темноте. Радар необходим для авиадиспетчеров, которые с его помощью определяют высоту самолёта и место, где он находится, далеко ли от аэропорта. С помощью радара и другие виды транспорта могут двигаться, не рискуя столкнуться. Радар может рассмотреть очень маленькие предметы размером с насекомых или большие, величиной с гору. Для прогнозирования погоды используются радары, которые могут обнаружить приближение грозовой тучи или урагана. Учёные применяют радар для изучения атмосферы и других планет. Он также необходим для космических полётов: с помощью радара диспетчер на Земле может проследить путь ракеты до выхода за орбиту. Радар широко применяется в военных целях, поскольку он может предупредить о приближении вражеских ракет, самолётов или субмарин. Работа радара похожа на звуковое эхо, он обнаруживает объекты, посылая в их сторону микроволны. Передатчик радара направляет микроволны в небо. Когда волны натыкаются на какое-то препятствие, например, на самолёт, часть их отражается назад, к радарной тарелке, она передаёт их на приёмное устройство, которое преобразует волны в электрический сигнал. Учитывая промежуток времени, который понадобился для возвращения отражённых волн, лазерная установка определяет расстояние до самолёта. Местонахождение самолёта высвечивается как яркое или мигающее пятно на дисплее. Антенны радара вращаются так, чтобы охватить волнами весь горизонт. Почти все большие самолёты снабжены радарами, предупреждающими их о находящихся поблизости самолётах и о надвигающемся шторме. Радар на борту корабля посылает микроволны вдоль поверхности воды так, чтобы они отразились от любого корабля или неожиданного препятствия на пути судна. Первую радарную установку создал учёный Роберт Ватсон-Ватт. В 1935 году Британское правительство поручило Ватсон-Ватту изобрести «лучи смерти» для отражения атак вражеских самолётов. Это оказалось невозможным, но, исследуя данную идею, учёный изобрёл радар для обнаружения вражеских самолётов. Многие люди считают, что радар сыграл очень важную роль во Второй мировой войне.
        """.trimIndent()
    )

    private var fullText: String = ""
    private var currentPosition = 0
    private var animator: ValueAnimator? = null
    private var selectedTextIndex = 0
    private var breakWordIndex = 0
    private lateinit var guideView: View

    private val breakWords = listOf(
        listOf("на новой"),
        listOf("которые поступали в", "290000"),
        listOf("сигнал.")
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_technique_detail, container, false)

        val techniqueName = arguments?.getString(ARG_TECHNIQUE_NAME)
        val titleTextView = view.findViewById<TextView>(R.id.technique_title)
        val descriptionTextView = view.findViewById<TextView>(R.id.technique_description)
        val animationTextView = view.findViewById<TextView>(R.id.animation_text)
        val startButton = view.findViewById<Button>(R.id.start_button)
        val backButton = view.findViewById<Button>(R.id.back_button)

        titleTextView.text = techniqueName

        val description = when (techniqueName) {
            "Чтение по диагонали" -> {
                val text = "Чтение по диагонали — это техника скорочтения, при которой взгляд движется по диагональной линии от верхнего левого угла к нижнему правому, охватывая ключевые слова и фразы. Этот метод помогает быстро выделить основную информацию, не задерживаясь на каждом слове.\n" +
                        "Чтобы правильно применять эту методику, ведите взгляд по диагонали сверху вниз, не фокусируясь на каждом слове, а замечая ключевые смысловые точки текста.\n" +
                        "Внимание сосредотачивается на заголовках, терминах, цифрах и выделенных фрагментах, а второстепенные слова и детали игнорируются, что ускоряет процесс чтения и восприятия материала."
                val spannable = SpannableString(text)

                spannable.setSpan(StyleSpan(Typeface.BOLD), 0, "Чтение по диагонали".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                spannable.setSpan(StyleSpan(Typeface.BOLD), text.indexOf("ведите взгляд по диагонали"), text.indexOf("ведите взгляд по диагонали") + "ведите взгляд по диагонали".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                spannable.setSpan(StyleSpan(Typeface.BOLD), text.indexOf("ключевые смысловые точки"), text.indexOf("ключевые смысловые точки") + "ключевые смысловые точки".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                spannable.setSpan(StyleSpan(Typeface.BOLD), text.indexOf("заголовках, терминах, цифрах и выделенных фрагментах"), text.indexOf("заголовках, терминах, цифрах и выделенных фрагментах") + "заголовках, терминах, цифрах и выделенных фрагментах".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                spannable
            }
            "Зигзагообразное чтение" -> "Метод, при котором глаза движутся зигзагом для захвата ключевой информации."
            "Вертикальное чтение" -> "Чтение текста сверху вниз по центру страницы."
            "Поиск ключевых слов" -> "Фокусировка только на ключевых словах и фразах."
            "Чтение \"блоками\"" -> "Восприятие текста целыми смысловыми блоками."
            "Периферийное чтение" -> "Использование бокового зрения для охвата большего объема текста."
            "Обратное чтение" -> "Чтение текста в обратном направлении для лучшего понимания."
            "Метод \"указки\"" -> "Использование пальца или ручки для направления взгляда."
            else -> "Описание техники в разработке"
        }
        descriptionTextView.text = description

        guideView = View(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(2, animationTextView.height)
            setBackgroundColor(Color.RED)
            rotation = 45f
            visibility = View.INVISIBLE
        }

        if (techniqueName == "Чтение по диагонали") {
            animationTextView.visibility = View.GONE

            startButton.setOnClickListener {
                descriptionTextView.visibility = View.GONE
                startButton.visibility = View.GONE
                animationTextView.visibility = View.VISIBLE
                (view as ViewGroup).addView(guideView)

                selectedTextIndex = Random.nextInt(sampleTexts.size)
                fullText = sampleTexts[selectedTextIndex].replace("\n", " ")
                Log.d("TechniqueDetail", "Selected text index: $selectedTextIndex, Full text: '$fullText'")
                currentPosition = 0
                breakWordIndex = 0

                animationTextView.gravity = android.view.Gravity.TOP
                animationTextView.post {
                    showNextTextPart(animationTextView, guideView)
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

    private fun showNextTextPart(textView: TextView, guideView: View) {
        if (currentPosition >= fullText.length) {
            guideView.visibility = View.INVISIBLE
            Log.d("TechniqueDetail", "Text ended, stopping animation")
            animator?.cancel()

            val currentText = textView.text.toString()
            textView.text = currentText // Убираем подсветку, оставляя последнюю часть
            return
        }

        val currentBreakWords = breakWords[selectedTextIndex]
        val breakWord = if (breakWordIndex < currentBreakWords.size) currentBreakWords[breakWordIndex] else ""
        val breakPosition = if (breakWord.isNotEmpty()) {
            val index = fullText.indexOf(breakWord, currentPosition)
            if (index == -1) fullText.length else index + breakWord.length
        } else {
            fullText.length
        }

        val partText = fullText.substring(currentPosition, breakPosition).trim()
        Log.d("TechniqueDetail", "Showing part: startPosition=$currentPosition, endPosition=$breakPosition, breakWord='$breakWord', text='$partText'")

        textView.text = partText
        startDiagonalAnimation(textView, guideView, breakPosition, partText)
    }

    private fun startDiagonalAnimation(textView: TextView, guideView: View, newPosition: Int, partText: String) {
        guideView.visibility = View.VISIBLE
        animator?.cancel()

        // Вычисляем количество слов в текущей части текста
        val wordCount = partText.split("\\s+".toRegex()).filter { it.isNotEmpty() }.size
        // Устанавливаем скорость: 200 мс на слово (можно настроить)
        val durationPerWord = 40L // 200 миллисекунд на слово
        val totalDuration = wordCount * durationPerWord

        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = totalDuration // Динамическая длительность
            var lastLine = -1

            addUpdateListener { animation ->
                val fraction = animation.animatedValue as Float
                val width = textView.width.toFloat()
                val visibleHeight = textView.height.toFloat()

                val layout = textView.layout
                val totalLines = layout?.lineCount ?: 1
                val lastLineTop = if (totalLines > 1) layout.getLineTop(totalLines - 1) else visibleHeight
                val heightExcludingLastLine = if (totalLines > 1) lastLineTop.toFloat() else visibleHeight

                val y = fraction * heightExcludingLastLine
                val x = fraction * width

                guideView.translationX = x - (guideView.width / 2) + textView.left
                guideView.translationY = textView.top.toFloat()

                val currentLine = highlightWordAtPosition(textView, x, y, lastLine)
                if (currentLine != -1) lastLine = currentLine
            }
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    currentPosition = newPosition
                    breakWordIndex++
                    Log.d("TechniqueDetail", "Animation ended, new currentPosition=$currentPosition, breakWordIndex=$breakWordIndex")
                    showNextTextPart(textView, guideView)
                }
            })
            start()
        }
    }

    private fun highlightWordAtPosition(textView: TextView, x: Float, y: Float, lastLine: Int): Int {
        val layout = textView.layout ?: return -1
        val visibleHeight = textView.height.toFloat()

        val adjustedY = y.coerceIn(0f, visibleHeight)
        val currentLine = layout.getLineForVertical(adjustedY.toInt())

        val totalLines = layout.lineCount
        if (currentLine == totalLines - 1) {
            return currentLine
        }

        if (currentLine <= lastLine) return currentLine

        val diagonalSlope = visibleHeight / textView.width.toFloat()
        val expectedX = adjustedY / diagonalSlope

        var closestOffset = -1
        var minDistance = Float.MAX_VALUE

        for (offset in layout.getLineStart(currentLine) until layout.getLineEnd(currentLine)) {
            if (textView.text[offset].isWhitespace()) continue

            val charLeft = layout.getPrimaryHorizontal(offset)
            val charRight = if (offset + 1 < textView.text.length) layout.getPrimaryHorizontal(offset + 1) else charLeft
            val charX = (charLeft + charRight) / 2

            val distance = abs(charX - expectedX)
            if (distance < minDistance) {
                minDistance = distance
                closestOffset = offset
            }
        }

        if (closestOffset != -1) {
            val text = textView.text.toString()
            var start = closestOffset
            var end = closestOffset

            while (start > 0 && !text[start - 1].isWhitespace()) start--
            while (end < text.length && !text[end].isWhitespace()) end++

            val spannable = SpannableString(text)
            spannable.removeSpan(BackgroundColorSpan(Color.YELLOW))
            spannable.setSpan(
                BackgroundColorSpan(Color.YELLOW),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            textView.text = spannable
        }

        return currentLine
    }

    override fun onDestroyView() {
        super.onDestroyView()
        animator?.cancel()
    }
}