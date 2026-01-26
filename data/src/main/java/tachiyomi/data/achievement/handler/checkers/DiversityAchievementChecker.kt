package tachiyomi.data.achievement.handler.checkers

import tachiyomi.data.handlers.anime.AnimeDatabaseHandler
import tachiyomi.data.handlers.manga.MangaDatabaseHandler

class DiversityAchievementChecker(
    private val mangaHandler: MangaDatabaseHandler,
    private val animeHandler: AnimeDatabaseHandler,
) {

    /**
     * Get total count of unique genres across both manga and anime library
     */
    suspend fun getGenreDiversity(): Int {
        val mangaGenres = mangaHandler.awaitList {
            mangasQueries.getLibraryGenres { genre ->
                genre
            }
        }

        val animeGenres = animeHandler.awaitList {
            animesQueries.getLibraryGenres { genre ->
                genre
            }
        }

        // Combine and parse unique genres from both manga and anime
        val allGenreStrings = mangaGenres + animeGenres
        return parseAndGetUniqueGenres(allGenreStrings)
    }

    /**
     * Get total count of unique sources across both manga and anime library
     */
    suspend fun getSourceDiversity(): Int {
        val mangaSources = mangaHandler.awaitList {
            mangasQueries.getLibrarySources { source ->
                source
            }
        }

        val animeSources = animeHandler.awaitList {
            animesQueries.getLibrarySources { source ->
                source
            }
        }

        // Combine and get unique sources from both manga and anime
        return (mangaSources + animeSources).distinct().size
    }

    /**
     * Get unique genres count from manga library only
     */
    suspend fun getMangaGenreDiversity(): Int {
        val mangaGenres = mangaHandler.awaitList {
            mangasQueries.getLibraryGenres { genre ->
                genre
            }
        }

        return parseAndGetUniqueGenres(mangaGenres)
    }

    /**
     * Get unique genres count from anime library only
     */
    suspend fun getAnimeGenreDiversity(): Int {
        val animeGenres = animeHandler.awaitList {
            animesQueries.getLibraryGenres { genre ->
                genre
            }
        }

        return parseAndGetUniqueGenres(animeGenres)
    }

    /**
     * Get unique sources count from manga library only
     */
    suspend fun getMangaSourceDiversity(): Int {
        val mangaSources = mangaHandler.awaitList {
            mangasQueries.getLibrarySources { source ->
                source
            }
        }

        return mangaSources.distinct().size
    }

    /**
     * Get unique sources count from anime library only
     */
    suspend fun getAnimeSourceDiversity(): Int {
        val animeSources = animeHandler.awaitList {
            animesQueries.getLibrarySources { source ->
                source
            }
        }

        return animeSources.distinct().size
    }

    /**
     * Parse genre strings (comma-separated) and return count of unique genres
     */
    private fun parseAndGetUniqueGenres(genreStrings: List<String?>): Int {
        return genreStrings
            .filterNotNull()
            .flatMap { genreString ->
                // Parse comma-separated genre strings
                genreString.split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
            }
            .distinct()
            .size
    }
}
