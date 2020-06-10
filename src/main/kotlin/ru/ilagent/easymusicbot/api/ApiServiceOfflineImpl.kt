package ru.il_agent.easymusic.services.api


class ApiServiceOfflineImpl : ApiService {
    override suspend fun search(query: String, offset: Int): SearchResult =
        SearchResult(
            (1..50)
                .map {
                    Audio(
                        "",
                        "",
                        "Test Artist",
                        "SuperSong",
                        45,
                        "",
                        "",
                        "",
                        "",
                        "",
                        emptyList()
                    )
                },
            false
        )


    override suspend fun getAudio(poorAudio: Audio): Audio =
        poorAudio.copy(url = "/sdcard/Downloads/test.mp3")
}