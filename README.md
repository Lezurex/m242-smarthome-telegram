# Telegram Bot for Smarthome with MQTT

## Requirements

- Java JDK 17

## Getting started

- Clone the repository

```bash
git clone https://github.com/Lezurex/m242-smarthome-telegram
```

- Change into repository

```bash
cd m242-smarthome-telegram/
```

- Copy config example and edit accordingly

```bash
cp config.properties.example config.properties
```

- Run gradle
- Fix gradle errors (mostly wrong version, java 17 requires gradle 7.2)

## Telegram Bot

You can create a new TelegramBot and acquire an API Token using BotFather. All information related to Telegram Bots can be found [here](https://core.telegram.org/bots#6-botfather).

## Links

- <https://github.com/eclipse/paho.mqtt.java>
- <https://github.com/pengrad/java-telegram-bot-api>
