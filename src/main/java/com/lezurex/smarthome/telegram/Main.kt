package com.lezurex.smarthome.telegram

import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.io.FileReader
import java.io.IOException
import java.util.*
import java.util.logging.ConsoleHandler
import java.util.logging.Level
import java.util.logging.Logger

object Main {
    private lateinit var logger: Logger
    private lateinit var config: Properties
    private fun loadConfig(): Boolean {
        config = Properties()
        try {
            FileReader("config.properties").use { fr ->
                config.load(fr)
                return true
            }
        } catch (e: IOException) {
            logger.log(Level.SEVERE, "Error loading config file", e)
        }
        return false
    }

    @Throws(InterruptedException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val ch = ConsoleHandler()
        ch.level = Level.ALL
        Logger.getGlobal().addHandler(ch)
        logger = Logger.getLogger("main")
        if (!loadConfig()) return
        logger.info("Config file loaded")
        val tnb = TelegramNotificationBot(config.getProperty("telegram-apikey"))
        tnb.toString()
        logger.info("TelegramBot started")
        val mqttClient = Mqtt(config.getProperty("mqtt-url"), "smarthome")
        try {
            mqttClient.start()
            mqttClient.subscribe("alp/m5core2/#")
            mqttClient.publish("M5Stack", "test")
        } catch (e: MqttException) {
            e.printStackTrace()
        }
        mqttClient.addHandler { _: String?, _: MqttMessage? -> }
    }
}