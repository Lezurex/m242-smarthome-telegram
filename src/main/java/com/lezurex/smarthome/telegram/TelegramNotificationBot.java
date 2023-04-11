package com.lezurex.smarthome.telegram;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import org.eclipse.paho.client.mqttv3.MqttException;

public class TelegramNotificationBot implements UpdatesListener {

    private static final List<String> rooms =
            List.of("livingroom", "kitchen", "bedroom", "hallway", "wardrobe");
    private static final List<String> modes = List.of("off", "on", "flashing", "party");

    private final TelegramBot bot;
    private final Mqtt mqttClient;

    public TelegramNotificationBot(String botToken, Mqtt mqttClient) {
        bot = new TelegramBot(botToken);
        this.mqttClient = mqttClient;

        bot.setUpdatesListener(this);
    }

    private void handleRoom(String room, Update update) {
        var parts = update.message().text().split("\\s+");
        if (parts.length != 3) {
            replyMessage(update, "Invalid command!");
            return;
        }
        switch (parts[1].toLowerCase()) {
            case "mode":
                if (!modes.contains(parts[2].toLowerCase())) {
                    replyMessage(update, "Please use a supported mode!");
                    return;
                }
                setValue(room, "mode", parts[2].toLowerCase());
                break;
            case "brightness":
                try {
                    var brightness = Integer.parseInt(parts[2]);
                    if (brightness > 100 || brightness < 0)
                        throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    replyMessage(update, "Please use a whole number between 0 and 100.");
                    return;
                }
                setValue(room, "brightness", parts[2]);
                break;
            case "color":
                setValue(room, "mode", parts[2]);
                break;
            default:
                replyMessage(update, "Not a valid property! Use mode, brightness or color!");
                return;
        }
        replyMessage(update, "Update sent successfully!");
    }

    private void setValue(String room, String key, String value) {
        try {
            mqttClient.sendMessage(String.format("smarthome/%s/%s", room, key), value);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void replyMessage(Update update, String message) {
        var reply = new SendMessage(update.message().chat().id(), message);
        bot.execute(reply);
    }

    @Override
    public int process(List<Update> updates) {
        updates.stream().filter(u -> u.message() != null).forEach((update -> {
            var room = rooms.stream()
                    .filter(r -> update.message().text().startsWith(String.format("/%s", r)))
                    .findFirst();
            if (room.isPresent()) {
                handleRoom(room.get(), update);
            }
        }));

        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
}
