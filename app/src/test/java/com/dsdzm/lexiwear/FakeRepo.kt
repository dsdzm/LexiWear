package com.dsdzm.lexiwear

import com.dsdzm.lexiwear.data.Collocation
import com.dsdzm.lexiwear.data.Example
import com.dsdzm.lexiwear.data.Form
import com.dsdzm.lexiwear.data.IndexEntry
import com.dsdzm.lexiwear.data.LexiRepository
import com.dsdzm.lexiwear.data.Meta
import com.dsdzm.lexiwear.data.Occurrence
import com.dsdzm.lexiwear.data.RelWord
import com.dsdzm.lexiwear.data.TbBook
import com.dsdzm.lexiwear.data.TbBookBrief
import com.dsdzm.lexiwear.data.TbUnit
import com.dsdzm.lexiwear.data.TbVersion
import com.dsdzm.lexiwear.data.TbWord
import com.dsdzm.lexiwear.data.WordEntry

class FakeRepo : LexiRepository {

    private val responsible = WordEntry(
        word = "responsible",
        uk = "rɪˈspɒnsəb(ə)l",
        us = "rɪˈspɑːnsəb(ə)l",
        pos = "adj",
        senses = listOf(
            "adj. （对某人、某事）负责的；（对事故、错误等）负有责任的，应承担责任的；可靠的",
            "adj. （工作或职位）重要的，责任重大的",
        ),
        forms = listOf(
            Form("比较级", "more responsible"),
            Form("最高级", "most responsible"),
        ),
        rel = listOf(
            RelWord("n.", "responsibility", "责任，职责；义务"),
            RelWord("adv.", "responsibly", "负责地，可信赖地"),
            RelWord("n.", "response", "响应；反应；回答"),
        ),
        examples = listOf(
            Example(
                "We are all entirely responsible for our actions, and for our reactions.",
                "我们都对自己的行为、自己的反应完全负责。",
            ),
            Example(
                "Our duty is to hold ourselves responsible to the people.",
                "我们的责任，是向人民负责。",
            ),
        ),
        collocations = listOf(
            Collocation("be responsible for", "对……负责，是造成……原因"),
        ),
        occurrences = listOf(
            Occurrence("人教版（2019）", "必修 第一册", "Unit 1"),
            Occurrence("外研版（2019）", "必修 第一册", "Unit 3"),
            Occurrence("译林版（2020）", "必修 第一册", "Unit 1"),
        ),
    )

    override suspend fun meta() = Meta(19177, 10, 70, 512, "2026-11-09")

    override suspend fun index(): List<IndexEntry> = listOf(
        IndexEntry("responsible", "adj. 负责的；有责任的；可靠的", "adj"),
        IndexEntry("responsibility", "n. 责任，职责；义务", "n"),
        IndexEntry("responsibly", "adv. 负责地，可信赖地", "adv"),
        IndexEntry("respond", "v. 回答；作出反应", "v"),
        IndexEntry("response", "n. 响应；反应；回答", "n"),
        IndexEntry("concentrate", "v. 集中；专心；浓缩", "v"),
    )

    override suspend fun search(query: String, limit: Int): List<IndexEntry> =
        index().filter { it.word.contains(query, ignoreCase = true) }.take(limit)

    override suspend fun word(word: String): WordEntry? =
        if (word.equals("responsible", ignoreCase = true)) responsible
        else if (word.equals("responsibility", ignoreCase = true))
            responsible.copy(
                word = "responsibility",
                pos = "n",
                senses = listOf("n. 责任，职责；义务；任务"),
                rel = emptyList(),
                examples = emptyList(),
                collocations = emptyList(),
                occurrences = emptyList(),
            )
        else null

    override suspend fun versions(): List<TbVersion> = listOf(
        TbVersion(
            "v0", "人教版（2019）", "新课标（2019/2020）", "人民教育出版社",
            listOf(
                TbBookBrief("b0", "必修 第一册", "高一", 6),
                TbBookBrief("b1", "必修 第二册", "高一", 5),
                TbBookBrief("b2", "必修 第三册", "高一", 5),
                TbBookBrief("b3", "选择性必修 第一册", "高二", 5),
            ),
        ),
        TbVersion(
            "v1", "外研版（2019）", "新课标（2019/2020）", "外语教学与研究出版社",
            listOf(
                TbBookBrief("b4", "必修 第一册", "高一", 6),
                TbBookBrief("b5", "必修 第二册", "高一", 6),
            ),
        ),
        TbVersion(
            "v2", "牛津上海版（上海现行）", "上海现行版", "上海教育出版社（牛津）",
            listOf(TbBookBrief("b6", "高一上", "高一", 6)),
        ),
    )

    override suspend fun book(id: String): TbBook = TbBook(
        id = id,
        name = "必修 第一册",
        grade = "高一",
        version = "人教版（2019）",
        units = listOf(
            TbUnit(
                "Welcome Unit",
                listOf(
                    TbWord("exchange", "n/v", "n. 交换；交流 v. 交换；兑换"),
                    TbWord("lecture", "n/v", "n. 讲座；教训 v. 开讲座；训斥"),
                    TbWord("registration", "n", "n. 登记，注册"),
                ),
            ),
            TbUnit(
                "Unit 1",
                listOf(
                    TbWord("teenage", "adj", "adj. 十几岁的，青少年的"),
                    TbWord("volunteer", "n/v", "n. 志愿者 v. 自愿做"),
                    TbWord("responsible", "adj", "adj. 负责的；有责任的"),
                ),
            ),
        ),
    )

    override suspend fun randomWord() = "responsible"
    override suspend fun wordOfTheDay() = "responsible"
    override fun recent(): List<String> = listOf("responsible", "concentrate")
    override fun addRecent(word: String) {}
}
