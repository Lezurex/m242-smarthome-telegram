package com.lezurex.smarthome.telegram;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Mqtt implements MqttCallback {

    private final String broker;
    private final String clientId;
    private final String username;
    private final String password;

    private static final int QOS = 2;

    private MemoryPersistence persistence;

    public Mqtt(String broker, String clientId) {
        this(broker, clientId, null, null);
    }

    public Mqtt(String broker, String clientId, String username, String password) {
        this.broker = broker;
        this.clientId = clientId;
        this.username = username;
        this.password = password;
        this.logger = Logger.getLogger("Mqtt");
    }

    private MqttClient client;

    Logger logger;
    private Thread controlThread;
    private volatile boolean isRunning;

    private ArrayList<BiConsumer<String, MqttMessage>> consumers = new ArrayList<>();


    public void sendMessage(String topic, String content) throws MqttException {
        MqttMessage message = new MqttMessage(content.getBytes());
        message.setQos(QOS);
        client.publish(topic, message);
        logger.info(() -> String.format("%s = %s", topic, message.toString()));
    }

    public void subscribe(String topic) throws MqttException {
        client.subscribe(topic);
    }

    public void addHandler(BiConsumer<String, MqttMessage> consumer) {
        consumers.add(consumer);
    }

    public void start() throws MqttException {
        persistence = new MemoryPersistence();
        client = new MqttClient(broker, clientId, persistence);
        client.setCallback(this);
        MqttConnectOptions connOpts = new MqttConnectOptions();
        if (username != null) {
            connOpts.setUserName(username);
            connOpts.setPassword(password.toCharArray());
        }
        connOpts.setCleanSession(true);
        logger.info(() -> String.format("Connecting to broker: %s", broker));
        client.connect(connOpts);
        logger.info("Connected");
        isRunning = true;
        controlThread = new Thread(() -> {
            try {
                while (isRunning) {
                    if (!client.isConnected()) {
                        client.reconnect();
                    }
                    Thread.sleep(1000l);
                }
            } catch (InterruptedException | MqttException e) {
                logger.log(Level.SEVERE, "", e);
                Thread.currentThread().interrupt();
            }
        });
        controlThread.start();

    }

    public void stop() throws MqttException {
        isRunning = false;
        client.disconnect();
        persistence.close();
        logger.info("Disconnected");
    }


    @Override
    public void connectionLost(Throwable cause) {
        logger.log(Level.SEVERE, "", cause);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        logger.fine(() -> String.format("Receveid: %s %s", topic, message.toString()));
        for (BiConsumer<String, MqttMessage> consumer : consumers) {
            consumer.accept(topic, message);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        logger.info(() -> String.format("Delivery %s is complete.", token.getMessageId()));
    }

    public void publish(String topic, String message) throws MqttException {
        client.publish(topic, new MqttMessage(message.getBytes(StandardCharsets.UTF_8)));
    }
}
