package com.telegram.bot;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import com.telegram.bot.exception.BotException;

public class App {

	private static final Logger LOGGER = Logger.getLogger(App.class.getName());

	public static void main(String[] args) throws BotException {
		ApiContextInitializer.init();

		TelegramBotsApi botsApi = new TelegramBotsApi();

		try {
			botsApi.registerBot(new ImageTextReader());
		} catch (TelegramApiException e) {
			LOGGER.error(e.getMessage(), e);
			throw new BotException("Error while registering bot");
		}

	}

}
