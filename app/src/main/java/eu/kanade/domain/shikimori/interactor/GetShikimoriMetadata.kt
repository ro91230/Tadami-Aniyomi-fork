package eu.kanade.domain.shikimori.interactor

import eu.kanade.domain.ui.UiPreferences
import eu.kanade.tachiyomi.data.track.shikimori.Shikimori
import eu.kanade.tachiyomi.data.track.shikimori.ShikimoriApi
import logcat.LogPriority
import logcat.logcat
import tachiyomi.core.common.util.lang.withIOContext
import tachiyomi.data.shikimori.ShikimoriMetadataCache
import tachiyomi.domain.entries.anime.model.Anime
import tachiyomi.domain.shikimori.model.ShikimoriMetadata
import tachiyomi.domain.track.anime.interactor.GetAnimeTracks

class GetShikimoriMetadata(
    private val metadataCache: ShikimoriMetadataCache,
    private val shikimori: Shikimori,
    private val shikimoriApi: ShikimoriApi,
    private val getAnimeTracks: GetAnimeTracks,
    private val preferences: UiPreferences,
) {

    suspend fun await(anime: Anime): ShikimoriMetadata? {
        // Check if disabled via settings
        if (!preferences.useShikimoriRating().get() &&
            !preferences.useShikimoriCovers().get()
        ) {
            return null
        }

        // Check cache first
        val cached = metadataCache.get(anime.id)
        if (cached != null && !cached.isStale()) {
            return cached
        }

        // Try to get from tracking
        val fromTracking = getFromTracking(anime)
        if (fromTracking != null) {
            metadataCache.upsert(fromTracking)
            return fromTracking
        }

        // Auto-search by title
        val fromSearch = searchAndCache(anime)
        if (fromSearch != null) {
            return fromSearch
        }

        // Cache "not found" to avoid re-searching
        cacheNotFound(anime)
        return null
    }

    private suspend fun getFromTracking(anime: Anime): ShikimoriMetadata? {
        return withIOContext {
            try {
                // Get Shikimori track for this anime
                val track = getAnimeTracks.await(anime.id)
                    .find { it.trackerId == shikimori.id }
                    ?: return@withIOContext null

                // Fetch full anime data from Shikimori
                val entry = shikimoriApi.getAnimeById(track.remoteId)

                ShikimoriMetadata(
                    animeId = anime.id,
                    shikimoriId = entry.id,
                    score = entry.score,
                    kind = entry.kind,
                    coverUrl = ShikimoriApi.BASE_URL + entry.image.preview,
                    searchQuery = "tracking:${track.remoteId}",
                    updatedAt = System.currentTimeMillis(),
                    isManualMatch = true,
                )
            } catch (e: Exception) {
                logcat(LogPriority.ERROR) { "Failed to get Shikimori data from tracking: ${e.message}" }
                null
            }
        }
    }

    private suspend fun searchAndCache(anime: Anime): ShikimoriMetadata? {
        return withIOContext {
            try {
                val results = shikimoriApi.searchAnime(anime.title)
                val firstResult = results.firstOrNull() ?: return@withIOContext null

                val metadata = ShikimoriMetadata(
                    animeId = anime.id,
                    shikimoriId = firstResult.remote_id,
                    score = firstResult.score,
                    kind = firstResult.publishing_type,
                    coverUrl = firstResult.cover_url,
                    searchQuery = anime.title,
                    updatedAt = System.currentTimeMillis(),
                    isManualMatch = false,
                )

                metadataCache.upsert(metadata)
                metadata
            } catch (e: Exception) {
                logcat(LogPriority.ERROR) { "Failed to search Shikimori: ${e.message}" }
                null
            }
        }
    }

    private suspend fun cacheNotFound(anime: Anime) {
        val notFound = ShikimoriMetadata(
            animeId = anime.id,
            shikimoriId = null,
            score = null,
            kind = null,
            coverUrl = null,
            searchQuery = anime.title,
            updatedAt = System.currentTimeMillis(),
            isManualMatch = false,
        )
        metadataCache.upsert(notFound)
    }
}
