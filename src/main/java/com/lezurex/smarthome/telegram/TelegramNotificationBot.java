package com.lezurex.smarthome.telegram;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.silentsoft.csscolor4j.Color;

public class TelegramNotificationBot implements UpdatesListener {

    private final TelegramBot bot;
    private final Mqtt mqttClient;

    public TelegramNotificationBot(String botToken, Mqtt mqttClient) {
        bot = new TelegramBot(botToken);
        this.mqttClient = mqttClient;

        bot.setUpdatesListener(this);
    }

    private void handleRoom(Room room, Update update) {
        var parts = update.message().text().split("\\s+");
        if (parts.length != 3) {
            replyMessage(update, "Invalid command!");
            return;
        }
        var property = Property.fromId(parts[1].toLowerCase());
        switch (property) {
            case MODE:
                if (Mode.isValid(parts[2].toLowerCase())) {
                    replyMessage(update, "Please use a supported mode!");
                    return;
                }
                setValue(room, Property.MODE, parts[2].toLowerCase());
                break;
            case BRIGHTNESS:
                try {
                    var brightness = Integer.parseInt(parts[2]);
                    if (brightness > 100 || brightness < 0)
                        throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    replyMessage(update, "Please use a whole number between 0 and 100.");
                    return;
                }
                setValue(room, Property.BRIGHTNESS, parts[2]);
                break;
            case COLOR:
                try {
                    var color = Color.valueOf(parts[2]);
                    setValue(room, Property.COLOR, toRGB(color));
                    replyMessage(update, "Color set to " + color.getHex() + "!");
                } catch (IllegalArgumentException e) {
                    replyMessage(update, "Please use a valid color!");
                    return;
                }
                break;
            default:
                replyMessage(update, "Not a valid property! Use mode, brightness or color!");
                return;
        }
        replyMessage(update, "Update sent successfully!");
    }

    private void handleMagicWords(Update update) {
        var message = update.message().text().toLowerCase();
        if (message.contains("sleep")) {
            Room.asList().forEach(r -> setValue(r, Property.MODE, "off"));
            setValue(Room.HALLWAY, Property.MODE, "on");
            setValue(Room.HALLWAY, Property.BRIGHTNESS, "10");
            setValue(Room.HALLWAY, Property.COLOR, toRGB(Color.valueOf("white")));
            replyMessage(update, "Good night!");
        } else if (message.contains("leaving")) {
            Room.asList().forEach(r -> setValue(r, Property.MODE, "off"));
            replyMessage(update, "Goodbye!");
        } else if (message.contains("party")) {
            Room.asList().forEach(r -> {
                setValue(r, Property.MODE, Mode.PARTY.getId());
                setValue(r, Property.BRIGHTNESS, "100");
            });
            replyMessage(update, "Let's party!");
        }
    }

    private String toRGB(Color color) {
        return String.format("%s,%s,%s", color.getRed(), color.getGreen(), color.getBlue());
    }

    private void setValue(Room room, Property property, String value) {
        try {
            mqttClient.sendMessage(String.format("smarthome/%s/%s", room.getId(), property.getId()),
                    value);
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
            var room = Room.fromId(update.message().text().substring(1).split("\\s+")[0]);
            if (room != null) {
                handleRoom(room, update);
            } else {
                handleMagicWords(update);
            }
        }));

        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
}
