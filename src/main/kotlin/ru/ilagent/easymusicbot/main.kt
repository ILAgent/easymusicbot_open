package ru.ilagent.easymusicbot

import me.ivmg.telegram.bot
import me.ivmg.telegram.dispatch
import me.ivmg.telegram.dispatcher.*
import ru.ilagent.easymusicbot.extensions.dispatchSessionsAware


fun main(args: Array<String>) {
    val bot = bot {
        token = TELEGRAM_TOKEN

        dispatch {
            telegramError { bot, error ->
                System.err.println(error.getErrorMessage())
            }
        }

        dispatchSessionsAware {

            commandController("start") {
                sendHelloMessage()
            }

            commandController("music") { request ->
                handleRequest(request)
            }

            textController { text ->
                handleRequest(text)
            }

            for (i in 0 until 50) {
                callbackController(callbackData(i)) {
                    handleSongRequest(i)
                }
            }

            callbackController(NEXT_PAGE) {
                showNextPage()
            }
        }
    }

    bot.startPolling()
}