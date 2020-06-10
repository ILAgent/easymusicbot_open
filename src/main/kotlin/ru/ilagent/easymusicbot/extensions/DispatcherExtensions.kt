package ru.ilagent.easymusicbot.extensions

import me.ivmg.telegram.Bot

fun Bot.Builder.dispatchSessionsAware(body: SessionsAwareDispatcher.() -> Unit): SessionsAwareDispatcher {
    val sessionsAwareDispatcher = SessionsAwareDispatcher(updater.dispatcher)
    return sessionsAwareDispatcher.apply(body)
}