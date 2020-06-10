package ru.ilagent.easymusicbot

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.ivmg.telegram.Bot
import me.ivmg.telegram.entities.InlineKeyboardButton
import me.ivmg.telegram.entities.InlineKeyboardMarkup
import me.ivmg.telegram.entities.ParseMode
import ru.il_agent.easymusic.services.api.ApiService
import ru.ilagent.easymusicbot.storage.ChatSessionState
import ru.ilagent.easymusicbot.storage.PAGE_SIZE
import ru.ilagent.easymusicbot.storage.Storage
import ru.ilagent.easymusicbot.storage.hasNextPage
import kotlin.math.min

class ChatSessionControllerImpl(
    private val chatId: Long,
    private val bot: Bot,
    private val api: ApiService,
    private val strings: Strings,
    private val storage: Storage
) : ChatSessionController {

    private var currentJob: Job? = null

    private suspend fun sessionState(): ChatSessionState = storage.getOrAddSession(chatId)

    override suspend fun handleRequest(request: String) {
        try {
            val res = api.search(request, 0)
            storage.setAudios(chatId, res.audios)
            showPage()
        } catch (ex: Throwable) {
            bot.sendMessage(chatId, strings.nothingFound.format(request))// todo use quotation
        }
    }

    override suspend fun showNextPage() {
        storage.nextPage(chatId)
        showPage()
    }

    override suspend fun handleSongRequest(audioIndex: Int) {
        val poorAudio = sessionState().offeredAudios.getOrNull(audioIndex)
        if (poorAudio == null) {
            bot.sendMessage(chatId, strings.listOutdated)
            return
        }
        val caption = "${poorAudio.title.fix()}   ${poorAudio.artist.fix()}"
        bot.sendMessage(
            chatId = chatId,
            text = strings.searchingFor.format(caption)
        )
        try {
            val audio = api.getAudio(poorAudio)
            bot.sendAudio(
                chatId = chatId,
                audio = audio.url,
                caption = caption
            )
        } catch (ex: Throwable) {
            bot.sendMessage(chatId, strings.cantGetAudion.format(caption))// todo use quotation
        }
    }

    override fun sendHelloMessage() {
        bot.sendMessage(
            chatId = chatId,
            text = strings.helloMessage
        )
    }

    fun perform(action: suspend ChatSessionControllerImpl.() -> Unit) {
        currentJob?.cancel()
        currentJob = GlobalScope.launch {
            try {
                action()
            } catch (ex: Throwable) {
                bot.sendMessage(
                    chatId = chatId,
                    text = "*${strings.error}:* $ex",
                    parseMode = ParseMode.MARKDOWN
                )
            }
        }
    }

    private suspend fun showPage() {
        val sessionState = sessionState()
        val audiosList = sessionState
            .pageIndexes()
            .joinToString(separator = "\r\n") { index ->
                val audio = sessionState.offeredAudios[index]
                "${index + 1}.  *${audio.title.fix()}*   ${audio.artist.fix()}"
            }

        bot.sendMessage(
            chatId = chatId,
            text = audiosList,
            parseMode = ParseMode.MARKDOWN,
            replyMarkup = sessionState.buttons()
        )
    }

    private fun ChatSessionState.buttons(): InlineKeyboardMarkup {
        val buttons = mutableListOf<MutableList<InlineKeyboardButton>>()

        var row = mutableListOf<InlineKeyboardButton>()
        for (index in pageIndexes()) {
            val colNum: Int = index % 5
            if (colNum == 0) {
                row = mutableListOf<InlineKeyboardButton>().also {
                    buttons.add(it)
                }
            }
            val button = InlineKeyboardButton(
                text = (index + 1).toString(),
                callbackData = callbackData(index)
            )
            row.add(button)
        }
        if (hasNextPage()) {
            buttons.add(
                mutableListOf(
                    InlineKeyboardButton(
                        text = strings.more,
                        callbackData = NEXT_PAGE
                    )
                )
            )
        }
        return InlineKeyboardMarkup(buttons)
    }

}


private fun String.fix() = replace("*", "")

private const val SONG_CHOSEN = "songChosen"
const val NEXT_PAGE = "nextPage"

fun callbackData(index: Int): String {
    val letter = ('a'.toInt() + index).toChar()
    return "${letter}_${SONG_CHOSEN}_$index"
}


fun ChatSessionState.pageIndexes(): IntRange {
    val start = currentPage * PAGE_SIZE
    val end = min(start + PAGE_SIZE, offeredAudios.size)
    return start until end
}