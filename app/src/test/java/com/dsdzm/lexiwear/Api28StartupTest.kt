package com.dsdzm.lexiwear

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.dsdzm.lexiwear.data.AssetLexiRepository
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * 真实启动路径验证：不替换仓库、不伪造数据，直接在 API 28 环境读取打包进 APK 的 assets。
 * 任何 GZIP / JSON / 资源缺失问题都会在这里抛出。
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class Api28StartupTest {

    @Test
    fun assetsReadable() = runBlocking {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        println("assets list: " + (ctx.assets.list("")?.joinToString(",")))
        println("lex list: " + (ctx.assets.list("lex")?.joinToString(",")))
        println("tb list: " + (ctx.assets.list("tb")?.joinToString(",")))

        val repo = AssetLexiRepository(ctx)
        val meta = repo.meta()
        println("META = $meta")

        val index = repo.index()
        println("INDEX size = ${index.size}, first = ${index.firstOrNull()}")

        val versions = repo.versions()
        println("VERSIONS = ${versions.size}: ${versions.take(3).map { it.name }}")

        val book = repo.book(versions.first().books.first().id)
        println("BOOK = ${book?.name} words=${book?.units?.sumOf { it.words.size }}")

        val w = repo.word("responsible")
        println("WORD = ${w?.word} senses=${w?.senses?.size} rel=${w?.rel?.size} ex=${w?.examples?.size}")

        val hit = repo.search("resp", 10)
        println("SEARCH = ${hit.map { it.word }}")
    }
}
