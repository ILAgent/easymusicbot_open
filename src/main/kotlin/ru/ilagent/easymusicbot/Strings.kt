package ru.ilagent.easymusicbot

interface Strings {
    val errorOccurred: String
    val helloMessage: String
    val more: String
    val error: String
    val nothingFound: String
    val cantGetAudion: String
    val searchingFor: String
    val listOutdated: String

    companion object {
        operator fun invoke(language: String?): Strings {
            val lang = language ?: "en"
            return when {
                lang.startsWith("ru") -> StringsRu
                else -> StringsEn
            }
        }
    }
}

private object StringsRu : Strings {
    override val errorOccurred: String
        get() = """
            Ой! Что-то пошло не так. Мне очень жаль :(
        """.trimIndent()

    override val helloMessage: String
        get() = """
            Привет!
            Меня зовут Easy Music Bot.
            Я призван сюда для того, что дать тебе любую песню, какую пожелаешь!
            Просто попробуй ввести название песни или исполнителся.
            Команда /help расскажет о командах, которые я знаю.
        """.trimIndent()

    override val more: String
        get() = "Ещё"

    override val error: String
        get() = "Ошибка"

    override val nothingFound: String
        get() = "К сожалению, ничего не удалось найти по запросу: %s"

    override val cantGetAudion: String
        get() = "К сожалению, не удалось получить запись: %s"

    override val searchingFor: String
        get() = "Загружаю \"%s\""

    override val listOutdated: String
        get() = "Список устарел. Пожалуйста, обновите поиск"

}

private object StringsEn : Strings {
    override val helloMessage: String
        get() = """
            Hi there!
            My name is Easy Music Bot.
            I'm here to provide you access to any music you can imagine!
            Just try to text the name of a song of an artist.
            Enter /help to see the list of my commands.   
        """.trimIndent()
    override val errorOccurred: String
        get() = """
            Oooops! Something went wrong. I'm so sorry :(
        """.trimIndent()

    override val more: String
        get() = "More"

    override val error: String
        get() = "Error"

    override val nothingFound: String
        get() = "Unfortunately, nothing has been found for: %s"

    override val cantGetAudion: String
        get() = "Unfortunately, the record \"%s\" can not be retrieved"

    override val searchingFor: String
        get() = "I'm loading \"%s\""

    override val listOutdated: String
        get() = "The list is outdated. Search again, please."
}