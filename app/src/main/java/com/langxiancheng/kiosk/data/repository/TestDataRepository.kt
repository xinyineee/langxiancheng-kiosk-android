package com.langxiancheng.kiosk.data.repository

import com.langxiancheng.kiosk.data.model.AnswerOption
import com.langxiancheng.kiosk.data.model.Drink
import com.langxiancheng.kiosk.data.model.Question
import com.langxiancheng.kiosk.data.model.TestResult
import com.langxiancheng.kiosk.data.model.WeightEntry
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository providing hardcoded test data: 5 questions and 6 drinks.
 * All data is defined offline — no network required.
 *
 * Also serves as the shared data holder for the last computed test result,
 * bridging QuestionViewModel → ResultViewModel across navigation.
 */
@Singleton
class TestDataRepository @Inject constructor() {

    /**
     * The last computed test result, set by QuestionViewModel when the test completes
     * and read by ResultViewModel when the result screen is displayed.
     * Volatile to ensure visibility across coroutine contexts.
     */
    @Volatile
    var lastResult: TestResult? = null
        private set

    /** The 6 special drinks with their copy and metadata. */
    val drinks: List<Drink> = listOf(
        Drink(
            id = "D1",
            name = "启程·梨想云",
            englishName = "Departure · Pearfect Cloud",
            tagline = "每天都是新的起跑线",
            heartCopy = "每一个起点，都藏着你最好的可能。今天，就是你的新起跑线。",
            colorHex = "#FFB347",
            emoji = "🍐"
        ),
        Drink(
            id = "D2",
            name = "探路·莓抹思慕雪",
            englishName = "Explorer · Berry Mousse Smoothie",
            tagline = "创业初期的复杂滋味，但值得",
            heartCopy = "路不是直的，味道也不是简单的。但复杂里，藏着最真实的你。",
            colorHex = "#C06C84",
            emoji = "🫐"
        ),
        Drink(
            id = "D3",
            name = "拼了·开心拿铁",
            englishName = "Pin Le · Happy Latte",
            tagline = "拼这一次，不问结果",
            heartCopy = "不是每次拼搏都有结果，但每次拼搏都有意义。这一杯，献给此刻的你。",
            colorHex = "#FF6B1A",
            emoji = "💪"
        ),
        Drink(
            id = "D4",
            name = "商米橙·破晓",
            englishName = "SUNMI Dawn · Daybreak",
            tagline = "从混沌到清晰，创业的第一缕光",
            heartCopy = "混乱之后，清晨终会来临。你已经走过最暗的那段路了。",
            colorHex = "#FF8C42",
            emoji = "🌅"
        ),
        Drink(
            id = "D5",
            name = "桂在路上·冷萃",
            englishName = "Gui Zai Lu Shang · Cold Brew",
            tagline = "路还长，但已经闻到桂花香",
            heartCopy = "不用急，慢慢来。走得稳的人，最终都能闻到桂花香。",
            colorHex = "#D4A574",
            emoji = "🍂"
        ),
        Drink(
            id = "D6",
            name = "上岸·奶砖拿铁",
            englishName = "Shore Finish · Milk Brick Latte",
            tagline = "熬过层层，开心上岸，这一杯，值得庆祝",
            heartCopy = "熬过来了。真的熬过来了。这一杯，是你给自己最好的礼物。",
            colorHex = "#E8C07D",
            emoji = "🎉"
        )
    )

    /** The 5 personality test questions with their options and weight mappings. */
    val questions: List<Question> = listOf(
        Question(
            id = "Q1",
            orderIndex = 0,
            questionText = "如果你的人生是一杯咖啡，现在处于哪个阶段？",
            options = listOf(
                AnswerOption(
                    label = "A",
                    optionText = "刚磨好豆子，满是香气和期待",
                    weights = listOf(WeightEntry("D1", 3), WeightEntry("D2", 1))
                ),
                AnswerOption(
                    label = "B",
                    optionText = "正在萃取中，有点苦但很专注",
                    weights = listOf(WeightEntry("D2", 3), WeightEntry("D4", 1))
                ),
                AnswerOption(
                    label = "C",
                    optionText = "快好了，闻到了那股味道",
                    weights = listOf(WeightEntry("D5", 3), WeightEntry("D4", 1))
                ),
                AnswerOption(
                    label = "D",
                    optionText = "已经端到手里，该好好喝了",
                    weights = listOf(WeightEntry("D6", 3), WeightEntry("D3", 1))
                )
            )
        ),
        Question(
            id = "Q2",
            orderIndex = 1,
            questionText = "有人问你\"你最近怎么样\"，你最诚实的回答是？",
            options = listOf(
                AnswerOption(
                    label = "A",
                    optionText = "还在想，脑子里一堆想法没落地",
                    weights = listOf(WeightEntry("D1", 2), WeightEntry("D4", 2))
                ),
                AnswerOption(
                    label = "B",
                    optionText = "乱得很，但好像也挺有意思的",
                    weights = listOf(WeightEntry("D2", 3), WeightEntry("D3", 1))
                ),
                AnswerOption(
                    label = "C",
                    optionText = "在熬，但方向越来越清晰了",
                    weights = listOf(WeightEntry("D5", 2), WeightEntry("D4", 2))
                ),
                AnswerOption(
                    label = "D",
                    optionText = "挺好的，刚过了一个难关",
                    weights = listOf(WeightEntry("D6", 3), WeightEntry("D3", 1))
                )
            )
        ),
        Question(
            id = "Q3",
            orderIndex = 2,
            questionText = "面对一个没把握但很诱人的机会，你会？",
            options = listOf(
                AnswerOption(
                    label = "A",
                    optionText = "直接冲，失败了再说",
                    weights = listOf(WeightEntry("D3", 3), WeightEntry("D1", 1))
                ),
                AnswerOption(
                    label = "B",
                    optionText = "先研究研究，再决定",
                    weights = listOf(WeightEntry("D4", 2), WeightEntry("D5", 2))
                ),
                AnswerOption(
                    label = "C",
                    optionText = "边走边看，先踏出第一步",
                    weights = listOf(WeightEntry("D2", 2), WeightEntry("D1", 2))
                ),
                AnswerOption(
                    label = "D",
                    optionText = "等时机更成熟再出手",
                    weights = listOf(WeightEntry("D5", 3), WeightEntry("D6", 1))
                )
            )
        ),
        Question(
            id = "Q4",
            orderIndex = 3,
            questionText = "你和朋友约好一起创业，对方突然退出，你？",
            options = listOf(
                AnswerOption(
                    label = "A",
                    optionText = "自己干，反正我本来就准备单飞",
                    weights = listOf(WeightEntry("D3", 3), WeightEntry("D1", 1))
                ),
                AnswerOption(
                    label = "B",
                    optionText = "调整心态，重新找搭档",
                    weights = listOf(WeightEntry("D2", 2), WeightEntry("D4", 2))
                ),
                AnswerOption(
                    label = "C",
                    optionText = "沉淀一下，想清楚再出发",
                    weights = listOf(WeightEntry("D5", 2), WeightEntry("D4", 2))
                ),
                AnswerOption(
                    label = "D",
                    optionText = "喝杯咖啡，今天先放下",
                    weights = listOf(WeightEntry("D6", 2), WeightEntry("D2", 2))
                )
            )
        ),
        Question(
            id = "Q5",
            orderIndex = 4,
            questionText = "浪险橙的 Slogan 是\"浪、险、成\"——你现在最像哪个字？",
            options = listOf(
                AnswerOption(
                    label = "A",
                    optionText = "浪——随心所欲，探索未知",
                    weights = listOf(WeightEntry("D1", 3), WeightEntry("D2", 1))
                ),
                AnswerOption(
                    label = "B",
                    optionText = "险——在悬崖边上，但不退缩",
                    weights = listOf(WeightEntry("D3", 3), WeightEntry("D4", 1))
                ),
                AnswerOption(
                    label = "C",
                    optionText = "成——已经看到结果的轮廓了",
                    weights = listOf(WeightEntry("D5", 2), WeightEntry("D6", 2))
                ),
                AnswerOption(
                    label = "D",
                    optionText = "还没想好，三个字我都想要",
                    weights = listOf(WeightEntry("D2", 2), WeightEntry("D1", 2))
                )
            )
        )
    )

    /**
     * Finds a drink by its ID.
     * @param drinkId The drink identifier (e.g., "D1")
     * @return The matching Drink, or null if not found
     */
    fun getDrinkById(drinkId: String): Drink? {
        return drinks.find { it.id == drinkId }
    }

    /**
     * Gets the total number of questions.
     */
    fun getQuestionCount(): Int = questions.size

    /**
     * Saves the computed test result so ResultViewModel can retrieve it.
     * Called by QuestionViewModel when the test completes (all questions answered or timeout).
     *
     * @param result The computed TestResult to persist
     */
    fun setLastResult(result: TestResult) {
        lastResult = result
    }

    /**
     * Clears the last test result. Called when returning to idle screen
     * to prevent stale data from appearing on a subsequent test.
     */
    fun clearLastResult() {
        lastResult = null
    }

    companion object {
        const val TAG = "TestDataRepository"
    }
}
