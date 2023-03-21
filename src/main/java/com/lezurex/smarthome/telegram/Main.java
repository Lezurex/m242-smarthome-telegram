package com.lezurex.smarthome.telegram;

import org.eclipse.paho.client.mqttv3.MqttException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static Logger logger;
    private static Properties config;

    private static boolean loadConfig() {
        config = new Properties();
        try (var fr = new FileReader("config.properties")) {
            config.load(fr);
            return true;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error loading config file", e);
        }
        return false;
    }

    public final static void main(String[] args) throws InterruptedException {
        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(Level.ALL);
        Logger.getGlobal().addHandler(ch);

        logger = Logger.getLogger("main");

        if (!loadConfig())
            return;

        logger.info("Config file loaded");

        TelegramNotificationBot tnb =
                new TelegramNotificationBot(config.getProperty("telegram-apikey"));

        logger.info("TelegramBot started");

        Mqtt mqttClient = new Mqtt(config.getProperty("mqtt-url"), "smarthome");
        try {
            mqttClient.start();
            mqttClient.subscribe("alp/m5core2/#");
            mqttClient.publish("M5Stack", "test");
        } catch (MqttException e) {
            e.printStackTrace();
        }

        mqttClient.addHandler((s, mqttMessage) -> {
            if (s.equals("alp/m5core2/temp")) {
            }
            if (s.equals("alp/m5core2/hum")) {
            }
        });
    }
}
