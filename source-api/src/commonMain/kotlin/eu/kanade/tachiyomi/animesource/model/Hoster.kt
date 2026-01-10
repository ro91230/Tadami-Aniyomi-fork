package eu.kanade.tachiyomi.animesource.model

import eu.kanade.tachiyomi.animesource.model.SerializableVideo.Companion.serialize
import eu.kanade.tachiyomi.animesource.model.SerializableVideo.Companion.toVideoList
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

open class Hoster(
    val hosterUrl: String = "",
    val hosterName: String = "",
    val videoList: List<Video>? = null,
    val internalData: String = "",
    val lazy: Boolean = false,
) {
    @Transient
    @Volatile
    var status: State = State.IDLE

    enum class State {
        IDLE,
        LOADING,
        READY,
        ERROR,
    }

    fun copy(
        hosterUrl: String = this.hosterUrl,
        hosterName: String = this.hosterName,
        videoList: List<Video>? = this.videoList,
        internalData: String = this.internalData,
        lazy: Boolean = this.lazy,
    ): Hoster {
        return Hoster(hosterUrl, hosterName, videoList, internalData, lazy)
    }

    companion object {
        const val NO_HOSTER_LIST = "no_hoster_list"

        private val TRANSLATION_PATTERN = Regex("""\(([^)]+)\)\s*$""")

        fun List<Video>.toHosterList(): List<Hoster> {
            val grouped = this.groupBy { video ->
                TRANSLATION_PATTERN.find(video.videoTitle)?.groupValues?.get(1) ?: NO_HOSTER_LIST
            }

            if (grouped.size <= 1 && grouped.containsKey(NO_HOSTER_LIST)) {
                return listOf(
                    Hoster(
                        hosterUrl = "",
                        hosterName = NO_HOSTER_LIST,
                        videoList = this,
                    ),
                )
            }

            return grouped.map { (translationName, videos) ->
                val cleanedVideos = videos.map { video ->
                    val cleanTitle = video.videoTitle.replace(TRANSLATION_PATTERN, "").trim()
                    video.copy(videoTitle = cleanTitle)
                }
                Hoster(
                    hosterUrl = "",
                    hosterName = translationName,
                    videoList = cleanedVideos,
                )
            }
        }
    }
}

@Serializable
data class SerializableHoster(
    val hosterUrl: String = "",
    val hosterName: String = "",
    val videoList: String? = null,
    val internalData: String = "",
    val lazy: Boolean = false,
) {
    companion object {
        fun List<Hoster>.serialize(): String =
            Json.encodeToString(
                this.map { host ->
                    SerializableHoster(
                        host.hosterUrl,
                        host.hosterName,
                        host.videoList?.serialize(),
                        host.internalData,
                        host.lazy,
                    )
                },
            )

        fun String.toHosterList(): List<Hoster> =
            Json.decodeFromString<List<SerializableHoster>>(this)
                .map { sHost ->
                    Hoster(
                        sHost.hosterUrl,
                        sHost.hosterName,
                        sHost.videoList?.toVideoList(),
                        sHost.internalData,
                        sHost.lazy,
                    )
                }
    }
}
