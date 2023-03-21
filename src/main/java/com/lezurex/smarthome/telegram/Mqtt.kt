package com.lezurex.smarthome.telegram

import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.nio.charset.StandardCharsets
import java.util.function.BiConsumer
import java.util.logging.Level
import java.util.logging.Logger

class Mqtt @JvmOverloads constructor(private val broker: String, private val clientId: String, private val username: String? = null, private val password: String? = null) : MqttCallback {
    private var persistence: MemoryPersistence? = null
    private var client: MqttClient? = null
    private var logger: Logger = Logger.getLogger("Mqtt")
    private var controlThread: Thread? = null

    @Volatile
    private var isRunning = false
    private val consumers = ArrayList<BiConsumer<String, MqttMessage>>()

    @Throws(MqttException::class)
    fun sendMessage(topic: String?, content: String) {
        val message = MqttMessage(content.toByteArray())
        message.qos = QOS
        client!!.publish(topic, message)
        logger.info { String.format("%s = %s", topic, message.toString()) }
    }

    @Throws(MqttException::class)
    fun subscribe(topic: String?) {
        client!!.subscribe(topic)
    }

    fun addHandler(consumer: BiConsumer<String, MqttMessage>) {
        consumers.add(consumer)
    }

    @Throws(MqttException::class)
    fun start() {
        persistence = MemoryPersistence()
        client = MqttClient(broker, clientId, persistence)
        client!!.setCallback(this)
        val connOpts = MqttConnectOptions()
        if (username != null) {
            connOpts.userName = username
            connOpts.password = password!!.toCharArray()
        }
        connOpts.isCleanSession = true
        logger.info { String.format("Connecting to broker: %s", broker) }
        client!!.connect(connOpts)
        logger.info("Connected")
        isRunning = true
        controlThread = Thread {
            try {
                while (isRunning) {
                    if (!client!!.isConnected) {
                        client!!.reconnect()
                    }
                    Thread.sleep(1000L)
                }
            } catch (e: InterruptedException) {
                logger.log(Level.SEVERE, "", e)
                Thread.currentThread().interrupt()
            } catch (e: MqttException) {
                logger.log(Level.SEVERE, "", e)
                Thread.currentThread().interrupt()
            }
        }
        controlThread!!.start()
    }

    @Throws(MqttException::class)
    fun stop() {
        isRunning = false
        client!!.disconnect()
        persistence!!.close()
        logger.info("Disconnected")
    }

    override fun connectionLost(cause: Throwable) {
        logger.log(Level.SEVERE, "", cause)
    }

    @Throws(Exception::class)
    override fun messageArrived(topic: String, message: MqttMessage) {
        logger.fine { String.format("Receveid: %s %s", topic, message.toString()) }
        for (consumer in consumers) {
            consumer.accept(topic, message)
        }
    }

    override fun deliveryComplete(token: IMqttDeliveryToken) {
        throw UnsupportedOperationException()
    }

    @Throws(MqttException::class)
    fun publish(topic: String?, message: String) {
        client!!.publish(topic, MqttMessage(message.toByteArray(StandardCharsets.UTF_8)))
    }

    companion object {
        private const val QOS = 2
    }
}