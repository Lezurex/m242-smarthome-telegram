package com.lezurex.smarthome.telegram;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TelegramNotificationBot implements UpdatesListener {

    private final TelegramBot bot;
    private final List<Long> users = Collections.synchronizedList(new ArrayList<Long>());

    public TelegramNotificationBot(String botToken) {
        bot = new TelegramBot(botToken);

        bot.setUpdatesListener(this);
    }

    public void sendTemperatureNotificationToAllUsers(double temperature) {
        for (Long user : users) {
            SendMessage reply = new SendMessage(user,
                    "The temperature changed to %.2f °C".formatted(temperature));
            bot.execute(reply);
        }
    }

    @Override
    public int process(List<Update> updates) {
        updates.stream().filter(u -> u.message() != null).forEach((update -> {
            SendMessage reply = new SendMessage(update.message().chat().id(), "Hi!");
            bot.execute(reply);
        }));

        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
}
