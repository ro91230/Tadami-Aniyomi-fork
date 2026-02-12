package eu.kanade.tachiyomi.ui.entries.novel

import eu.kanade.tachiyomi.novelsource.NovelSource
import eu.kanade.tachiyomi.source.novel.NovelSiteSource
import eu.kanade.tachiyomi.source.novel.NovelWebUrlSource
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class NovelScreenUrlResolverTest {

    @Test
    fun `returns absolute novel url as is`() {
        runBlocking {
            val resolved = resolveNovelEntryWebUrl(
                novelUrl = "https://ranobelib.me/ru/book-slug",
                source = null,
            )

            resolved shouldBe "https://ranobelib.me/ru/book-slug"
        }
    }

    @Test
    fun `resolves relative novel url via source web resolver`() {
        runBlocking {
            val source = object : NovelSource, NovelWebUrlSource {
                override val id: Long = 1L
                override val name: String = "Test"
                override suspend fun getNovelWebUrl(novelPath: String): String? {
                    return "https://ranobelib.me$novelPath"
                }
                override suspend fun getChapterWebUrl(chapterPath: String, novelPath: String?): String? = null
            }

            val resolved = resolveNovelEntryWebUrl(
                novelUrl = "/ru/book-slug",
                source = source,
            )

            resolved shouldBe "https://ranobelib.me/ru/book-slug"
        }
    }

    @Test
    fun `falls back to site url when source web resolver returns null`() {
        runBlocking {
            val source = object : NovelSource, NovelWebUrlSource, NovelSiteSource {
                override val id: Long = 1L
                override val name: String = "Test"
                override val siteUrl: String? = "https://ranobelib.me"
                override suspend fun getNovelWebUrl(novelPath: String): String? = null
                override suspend fun getChapterWebUrl(chapterPath: String, novelPath: String?): String? = null
            }

            val resolved = resolveNovelEntryWebUrl(
                novelUrl = "/ru/book-slug",
                source = source,
            )

            resolved shouldBe "https://ranobelib.me/ru/book-slug"
        }
    }

    @Test
    fun `returns null for blank novel url`() {
        runBlocking {
            val resolved = resolveNovelEntryWebUrl(
                novelUrl = "   ",
                source = null,
            )

            resolved shouldBe null
        }
    }
}
