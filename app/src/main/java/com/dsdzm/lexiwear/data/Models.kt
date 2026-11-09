package com.dsdzm.lexiwear.data

data class IndexEntry(val word: String, val brief: String, val pos: String)

data class Form(val name: String, val value: String)

data class RelWord(val pos: String, val word: String, val tran: String)

data class Example(val en: String, val zh: String)

data class Collocation(val phrase: String, val meaning: String)

data class Occurrence(val version: String, val book: String, val unit: String)

data class WordEntry(
    val word: String,
    val uk: String,
    val us: String,
    val pos: String,
    val senses: List<String>,
    val forms: List<Form>,
    val rel: List<RelWord>,
    val examples: List<Example>,
    val collocations: List<Collocation> = emptyList(),
    val occurrences: List<Occurrence> = emptyList(),
)

data class TbBookBrief(val id: String, val name: String, val grade: String, val units: Int)

data class TbVersion(
    val id: String,
    val name: String,
    val group: String,
    val press: String,
    val books: List<TbBookBrief>,
)

data class TbWord(val word: String, val pos: String, val meaning: String)

data class TbUnit(val name: String, val words: List<TbWord>)

data class TbBook(
    val id: String,
    val name: String,
    val grade: String,
    val version: String,
    val units: List<TbUnit>,
)

data class Meta(
    val entries: Int,
    val versions: Int,
    val books: Int,
    val units: Int,
    val builtAt: String,
)
