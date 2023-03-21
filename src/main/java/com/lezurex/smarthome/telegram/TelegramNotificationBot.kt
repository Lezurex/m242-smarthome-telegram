package com.lezurex.smarthome.telegram

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.UpdatesListener
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.request.SendMessage
import java.util.*

class TelegramNotificationBot(botToken: String?) : UpdatesListener {
    private val bot: TelegramBot
    private val users = Collections.synchronizedList(ArrayList<Long>())

    init {
        bot = TelegramBot(botToken)
        bot.setUpdatesListener(this)
    }

    fun sendTemperatureNotificationToAllUsers(temperature: Double) {
        for (user in users) {
            val reply = SendMessage(user, "The temperature changed.")
            bot.execute(reply)
        }
    }

    override fun process(updates: List<Update>): Int {
        updates.stream().filter { u: Update -> u.message() != null }.forEach { update: Update ->
            val reply = SendMessage(update.message().chat().id(), "Hi!")
            bot.execute(reply)
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL
    }
}