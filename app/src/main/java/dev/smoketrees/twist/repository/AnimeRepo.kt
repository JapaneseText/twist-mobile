package dev.smoketrees.twist.repository


import dev.smoketrees.twist.api.anime.AnimeWebClient
import dev.smoketrees.twist.db.AnimeDao
import dev.smoketrees.twist.db.AnimeDetailsDao
import dev.smoketrees.twist.db.TrendingAnimeDao
import dev.smoketrees.twist.model.twist.*
import dev.smoketrees.twist.utils.Messages

class AnimeRepo(
    val webClient: AnimeWebClient,
    private val animeDao: AnimeDao,
    private val episodeDao: AnimeDetailsDao,
    private val trendingAnimeDao: TrendingAnimeDao
) : BaseRepo() {

    fun getAllDbAnime() = animeDao.getAllAnime()
    suspend fun getAllNetworkAnime() = webClient.getAllAnime()
    suspend fun saveAnime(animeItems: List<AnimeItem>) = animeDao.saveAnime(animeItems)
    suspend fun saveTrendingAnime(animeItems: List<TrendingAnimeItem>) =
        trendingAnimeDao.saveTrendingAnime(animeItems)


    suspend fun getNetworkTrendingAnime(limit: Int) = webClient.getTrendingAnime(limit)

    fun getDbTrendingAnime() = trendingAnimeDao.getTrendingAnime()

    fun getMotd() = makeRequest {
        webClient.getMotd()
    }

    fun getAnimeDetails(name: String, id: Int) = makeRequestAndSave(
        databaseQuery = { episodeDao.getAnimeDetails(id) },
        networkCall = {
            saveEpisodeDetails(
                webClient.getAnimeDetails(name)
            )
        },
        saveCallResult = {
            episodeDao.saveAnimeDetails(it)
        }
    )

    private fun saveEpisodeDetails(
        episodeResult: Result<AnimeDetails>
    ): Result<AnimeDetailsEntity> {
        return if (episodeResult.status == Result.Status.SUCCESS) {
            Result.success(
                getAnimeDetailsEntity(episodeResult.data!!)
            )
        } else {
            Result.error(Messages.Message(null, ""))
        }
    }

    fun getAnimeByIds(ids: List<Int>) = animeDao.getAnimeByIds(ids)

    // TODO: add support for this data to nejire and use it in the app
    private fun getAnimeDetailsEntity(
        episodeDetails: AnimeDetails
    ) = AnimeDetailsEntity(
        airing = episodeDetails.ongoing == 1,
        //endDate = result?.endDate,
        //episodes = result?.episodes,
        imageUrl = episodeDetails.extension?.posterImage,
        id = episodeDetails.id,
        //malId = result?.malId,
        //members = result?.members,
        //rated = result?.rated,
        score = episodeDetails.extension?.avgScore,
        //startDate = result?.startDate,
        synopsis = episodeDetails.description,
        title = episodeDetails.title,
        //type = result?.type,
        //url = result?.url,
        episodeList = episodeDetails.episodes!!
    )

    fun getAnimeSources(animeName: String) = makeRequest {
        webClient.getAnimeSources(animeName)
    }

    fun kitsuRequest(pageLimit: Int, sort: String, offset: Int) = makeRequest {
        webClient.kitsuRequest(pageLimit, sort, offset)
    }

    fun signIn(loginDetails: LoginDetails) = makeRequest {
        webClient.signIn(loginDetails)
    }

    fun signUp(registerDetails: RegisterDetails) = makeRequest {
        webClient.signUp(registerDetails)
    }

    fun getUserLibrary(jwt: String) = makeRequest {
        webClient.getUserLibrary(jwt)
    }

    suspend fun getUserLibrarySync(jwt: String) = webClient.getUserLibrary(jwt)

    suspend fun saveWatchedEpisodes(episodes: List<LibraryEpisode>) =
        animeDao.saveWatchedEpisodes(episodes)

    fun getWatchedEpisodes(id: Int) = animeDao.getWatchedEpisodes(id)

    fun updateUserLibrary(jwt: String, body: PatchLibRequest) = makeRequest {
        webClient.updateUserLibrary(jwt, body)
    }
}