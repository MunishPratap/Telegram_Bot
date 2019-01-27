package com.telegram.bot.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.telegram.bot.constants.BotConstants;
import com.telegram.bot.exception.BotException;

public class Utilities {
	private static final Logger LOGGER = Logger.getLogger(Utilities.class.getName());

	private Utilities() {
	}

	public static String getProperty(String key) throws BotException {
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = Utilities.class.getResourceAsStream(BotConstants.CONFIG_FILE);
			prop.load(input);
		} catch (IOException | NullPointerException e) {
			LOGGER.error(e.getMessage(), e);
			throw new BotException("Error while reading property file.");
		}
		return prop.getProperty(key);
	}
}
