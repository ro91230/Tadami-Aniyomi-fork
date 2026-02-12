package eu.kanade.presentation.browse.novel

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class BrowseNovelSourceScreenTest {

    @Test
    fun `novelBrowseItemKey is unique for same url with different indices`() {
        val first = novelBrowseItemKey(url = "/novel/infinity-is-my-affinity", index = 4)
        val second = novelBrowseItemKey(url = "/novel/infinity-is-my-affinity", index = 9)

        (first == second) shouldBe false
    }

    @Test
    fun `novelBrowseItemKey keeps key deterministic`() {
        val key = novelBrowseItemKey(url = "/novel/a", index = 2)
        key shouldBe "novel//novel/a#2"
    }

    @Test
    fun `novelBrowseItemKey handles null url`() {
        val key = novelBrowseItemKey(url = null, index = 3)
        key shouldBe "novel/#3"
    }
}

