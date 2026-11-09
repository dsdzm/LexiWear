package com.dsdzm.lexiwear.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.zip.GZIPInputStream
import java.util.LinkedHashMap

interface LexiRepository {
    suspend fun meta(): Meta
    suspend fun index(): List<IndexEntry>
    suspend fun search(query: String, limit: Int = 40): List<IndexEntry>
    suspend fun word(word: String): WordEntry?
    suspend fun versions(): List<TbVersion>
    suspend fun book(id: String): TbBook?
    suspend fun randomWord(): String?
    suspend fun wordOfTheDay(): String?
    fun recent(): List<String>
    fun addRecent(word: String)
}

class AssetLexiRepository(private val context: Context) : LexiRepository {

    private val prefs = context.getSharedPreferences("lexiwear", Context.MODE_PRIVATE)

    @Volatile private var indexCache: List<IndexEntry>? = null
    @Volatile private var metaCache: Meta? = null
    @Volatile private var versionCache: List<TbVersion>? = null
    @Volatile private var enrichCache: Map<String, JSONObject>? = null

    private val shards = object : LinkedHashMap<String, Map<String, WordEntry>>(8, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Map<String, WordEntry>>) =
            size > 6
    }
    private val bookCache = object : LinkedHashMap<String, TbBook>(4, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, TbBook>) = size > 3
    }

    private fun readGz(name: String): String = context.assets.open(name).use { input ->
        GZIPInputStream(input).bufferedReader(Charsets.UTF_8).use { it.readText() }
    }

    private fun readPlain(name: String): String =
        context.assets.open(name).bufferedReader(Charsets.UTF_8).use { it.readText() }

    override suspend fun meta(): Meta = withContext(Dispatchers.IO) {
        metaCache ?: run {
            val o = JSONObject(readPlain("meta.json"))
            Meta(
                entries = o.optInt("entries"),
                versions = o.optInt("versions"),
                books = o.optInt("books"),
                units = o.optInt("units"),
                builtAt = o.optString("builtAt"),
            ).also { metaCache = it }
        }
    }

    override suspend fun index(): List<IndexEntry> = withContext(Dispatchers.IO) {
        indexCache ?: run {
            val arr = JSONArray(readGz("lex/index.json.gz"))
            val list = ArrayList<IndexEntry>(arr.length())
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(IndexEntry(o.optString("w"), o.optString("b"), o.optString("p")))
            }
            list.also { indexCache = it }
        }
    }

    override suspend fun search(query: String, limit: Int): List<IndexEntry> = withContext(Dispatchers.IO) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return@withContext emptyList()
        val all = index()
        val starts = ArrayList<IndexEntry>()
        val contains = ArrayList<IndexEntry>()
        for (e in all) {
            val w = e.word.lowercase()
            if (w.startsWith(q)) {
                starts.add(e)
                if (starts.size >= limit) break
            } else if (w.contains(q) && contains.size < limit) {
                contains.add(e)
            }
        }
        (starts + contains).take(limit)
    }

    private fun loadEnrich(): Map<String, JSONObject> {
        enrichCache?.let { return it }
        val o = JSONObject(readGz("lex/enrich.json.gz"))
        val map = HashMap<String, JSONObject>(o.length() * 2)
        val keys = o.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            map[k.lowercase()] = o.getJSONObject(k)
        }
        enrichCache = map
        return map
    }

    private fun loadShard(key: String): Map<String, WordEntry> {
        val o = JSONObject(readGz("lex/$key.json.gz"))
        val map = HashMap<String, WordEntry>(o.length() * 2)
        val keys = o.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            map[k.lowercase()] = parseWord(o.getJSONObject(k))
        }
        return map
    }

    private fun parseWord(o: JSONObject): WordEntry {
        val senses = o.optJSONArray("s").toStringList()
        val forms = ArrayList<Form>()
        o.optJSONArray("f")?.let { arr ->
            for (i in 0 until arr.length()) {
                val f = arr.optJSONObject(i) ?: continue
                forms.add(Form(f.optString("n"), f.optString("v")))
            }
        }
        val rel = ArrayList<RelWord>()
        o.optJSONArray("r")?.let { arr ->
            for (i in 0 until arr.length()) {
                val r = arr.optJSONObject(i) ?: continue
                rel.add(RelWord(r.optString("p"), r.optString("w"), r.optString("t")))
            }
        }
        val examples = ArrayList<Example>()
        o.optJSONArray("e")?.let { arr ->
            for (i in 0 until arr.length()) {
                val e = arr.optJSONObject(i) ?: continue
                examples.add(Example(e.optString("en"), e.optString("zh")))
            }
        }
        return WordEntry(
            word = o.optString("w"),
            uk = o.optString("uk"),
            us = o.optString("us"),
            pos = o.optString("p"),
            senses = senses,
            forms = forms,
            rel = rel,
            examples = examples,
        )
    }

    override suspend fun word(word: String): WordEntry? = withContext(Dispatchers.IO) {
        val key = word.trim().lowercase()
        if (key.isEmpty()) return@withContext null
        val shardKey = if (key[0] in 'a'..'z') key[0].toString() else "_"
        val shard = shards[shardKey] ?: loadShard(shardKey).also { shards[shardKey] = it }
        val base = shard[key] ?: return@withContext null
        val en = try { loadEnrich()[key] } catch (t: Throwable) { null }
        if (en == null) return@withContext base
        val cols = ArrayList<Collocation>()
        en.optJSONArray("c")?.let { arr ->
            for (i in 0 until arr.length()) {
                val c = arr.optJSONObject(i) ?: continue
                cols.add(Collocation(c.optString("p"), c.optString("m")))
            }
        }
        val occ = ArrayList<Occurrence>()
        en.optJSONArray("o")?.let { arr ->
            for (i in 0 until arr.length()) {
                val c = arr.optJSONObject(i) ?: continue
                occ.add(Occurrence(c.optString("v"), c.optString("b"), c.optString("u")))
            }
        }
        base.copy(collocations = cols, occurrences = occ)
    }

    override suspend fun versions(): List<TbVersion> = withContext(Dispatchers.IO) {
        versionCache ?: run {
            val root = JSONObject(readGz("tb/index.json.gz"))
            val arr = root.optJSONArray("versions") ?: JSONArray()
            val list = ArrayList<TbVersion>(arr.length())
            for (i in 0 until arr.length()) {
                val v = arr.getJSONObject(i)
                val bs = ArrayList<TbBookBrief>()
                v.optJSONArray("books")?.let { ba ->
                    for (j in 0 until ba.length()) {
                        val b = ba.getJSONObject(j)
                        bs.add(
                            TbBookBrief(
                                id = b.optString("id"),
                                name = b.optString("name"),
                                grade = b.optString("grade"),
                                units = b.optInt("units"),
                            )
                        )
                    }
                }
                list.add(
                    TbVersion(
                        id = v.optString("id"),
                        name = v.optString("name"),
                        group = v.optString("group"),
                        press = v.optString("press"),
                        books = bs,
                    )
                )
            }
            list.also { versionCache = it }
        }
    }

    override suspend fun book(id: String): TbBook? = withContext(Dispatchers.IO) {
        bookCache[id] ?: run {
            val o = try { JSONObject(readGz("tb/$id.json.gz")) } catch (t: Throwable) { return@withContext null }
            val units = ArrayList<TbUnit>()
            o.optJSONArray("units")?.let { ua ->
                for (i in 0 until ua.length()) {
                    val u = ua.getJSONObject(i)
                    val ws = ArrayList<TbWord>()
                    u.optJSONArray("words")?.let { wa ->
                        for (j in 0 until wa.length()) {
                            val w = wa.getJSONObject(j)
                            ws.add(TbWord(w.optString("w"), w.optString("p"), w.optString("m")))
                        }
                    }
                    units.add(TbUnit(u.optString("name"), ws))
                }
            }
            TbBook(
                id = o.optString("id"),
                name = o.optString("name"),
                grade = o.optString("grade"),
                version = o.optString("version"),
                units = units,
            ).also { bookCache[id] = it }
        }
    }

    override suspend fun randomWord(): String? = withContext(Dispatchers.IO) {
        val all = index().filter { it.word.length > 4 && !it.word.contains(' ') }
        if (all.isEmpty()) null else all[(Math.random() * all.size).toInt()].word
    }

    override suspend fun wordOfTheDay(): String? = withContext(Dispatchers.IO) {
        val all = index().filter { it.word.length > 4 && !it.word.contains(' ') }
        if (all.isEmpty()) null else {
            val day = System.currentTimeMillis() / 86_400_000L
            all[((day * 7919) % all.size).toInt()].word
        }
    }

    override fun recent(): List<String> =
        prefs.getString("recent", "").orEmpty().split('\n').filter { it.isNotBlank() }.take(8)

    override fun addRecent(word: String) {
        val list = recent().toMutableList()
        list.remove(word)
        list.add(0, word)
        prefs.edit().putString("recent", list.take(8).joinToString("\n")).apply()
    }
}

private fun JSONArray?.toStringList(): List<String> {
    if (this == null) return emptyList()
    val list = ArrayList<String>(length())
    for (i in 0 until length()) {
        val s = optString(i)
        if (s.isNotBlank()) list.add(s)
    }
    return list
}
