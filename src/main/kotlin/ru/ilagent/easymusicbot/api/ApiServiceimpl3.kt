package ru.il_agent.easymusic.services.api

import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

const val API_URL = "https://api.xn--41a.ws"
const val API = "/api.php?key=4504d9bec40854832fb49f3104c62819"

private const val SEARCH3 = "$API&method=search&v=3"
private const val DECODE = "$API&method=decode"

private interface MusicApi3 {
    @GET(SEARCH3)
    suspend fun search(
        @Query("q") query: String,
        @Query("offset") offset: Int = 0
    ): String

    @GET(DECODE)
    suspend fun decode(
        @Query("id") id: String,
        @Query("encoded_link") encodedLink: String,
        @Query("anchor") anchor: String
    ): String
}

class ApiServiceImpl3 : ApiService {

    private val musicApi: MusicApi3 = Retrofit.Builder()
        .baseUrl(API_URL)
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()
        .create(MusicApi3::class.java)

    override suspend fun search(query: String, offset: Int): SearchResult {
        val response = musicApi.search(query, offset)

        val fixedResponse = if (response.startsWith("{")) {
            response
        } else {
            "{\"list\":$response}"
        }
        val json = JSONObject(fixedResponse)
        val root = json.getJSONArray("list")
        val list = root.getJSONArray(0)
        val authorId = root.getJSONObject(1).getInt("AUDIO_ITEM_INDEX_AUTHOR_ID")
        val audios = (0 until list.length())
            .map { list.getJSONObject(it) }
            .map {
                Audio(
                    id = it.getString("audio_id"),
                    ownerId = "",
                    url = "",
                    title = it.getString("title"),
                    artist = it.getString("artist"),
                    duration = it.getInt("duration"),
                    albumId = "",
                    authorId = authorId.toString(),
                    lyricsId = "",
                    hashes = it.getString("mp3"),
                    images = listOf(it.getString("img"))
                )
            }
        val filteredAudios = audios
            .filter {
                it.id.isNotEmpty() && it.hashes.isNotEmpty()
            }
        return SearchResult(filteredAudios, offset == 0 && audios.size == PAGE_SIZE)

    }


    override suspend fun getAudio(poorAudio: Audio): Audio {
        if (poorAudio.url.isNotEmpty()) {
            return poorAudio
        }
        val parts = poorAudio.hashes.split("#")
        val decoded = musicApi.decode(poorAudio.authorId, parts[0], parts[1])
        poorAudio.url = decoded
        return poorAudio
    }
}

const val PAGE_SIZE = 50