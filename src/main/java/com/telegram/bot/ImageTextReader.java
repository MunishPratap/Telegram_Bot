package com.telegram.bot;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.methods.GetFile;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.PhotoSize;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import com.telegram.bot.constants.BotConstants;
import com.telegram.bot.exception.BotException;
import com.telegram.bot.util.AWSRekognitionBotUtil;
import com.telegram.bot.util.Utilities;
import com.vdurmont.emoji.EmojiParser;

public class ImageTextReader extends TelegramLongPollingBot {

	private static final Logger LOGGER = Logger.getLogger(ImageTextReader.class.getName());

	@Override
	public String getBotToken() {
		try {
			return Utilities.getProperty(BotConstants.BOT_TOKEN);
		} catch (BotException e) {
			LOGGER.error(e.getMessage());
			return null;
		}
	}

	@Override
	public String getBotUsername() {
		try {
			return Utilities.getProperty(BotConstants.BOT_TOKEN);
		} catch (BotException e) {
			LOGGER.error(e.getMessage());
			return null;
		}
	}

	@Override
	public void onUpdateReceived(Update update) {
		long chatId = update.getMessage().getChatId();
		if (update.hasMessage() && (update.getMessage().getText().trim().equalsIgnoreCase("/start")
				|| update.getMessage().getText().trim().equalsIgnoreCase("hi"))) {
			try {
				welcomeReply(chatId);
			} catch (BotException e) {
				LOGGER.error(e.getMessage());
			}
		} else if (update.hasMessage() && (update.getMessage().getText().trim().toLowerCase().contains("who are you")
				|| update.getMessage().getText().trim().toLowerCase().contains("who are u")
				|| update.getMessage().getText().trim().toLowerCase().contains("who r u")
				|| update.getMessage().getText().trim().toLowerCase().contains("who r you")
				|| update.getMessage().getText().trim().toLowerCase().contains("what is your name")
				|| update.getMessage().getText().trim().toLowerCase().contains("what's your name")
				|| update.getMessage().getText().trim().toLowerCase().contains("what's ur name")
				|| update.getMessage().getText().trim().toLowerCase().contains("what is ur name"))) {
			try {
				introReply(chatId);
			} catch (BotException e) {
				LOGGER.error(e.getMessage());
			}
		} else if (update.hasMessage() && hasEmoji(update)) {
			try {
				replyToEmoji(update, chatId);
			} catch (BotException e) {
				LOGGER.error(e.getMessage());
			}
		} else if (update.hasMessage() && update.getMessage().hasPhoto()) {
			try {
				replyToPhoto(update, chatId);
			} catch (BotException e) {
				LOGGER.error(e.getMessage());
			}
		} else if (update.hasMessage() && update.getMessage().hasText()) {
			try {
				replyToText(update, chatId);
			} catch (BotException e) {
				LOGGER.error(e.getMessage());
			}
		} else {
			try {
				SendMessage message = new SendMessage().setChatId(chatId)
						.setText("Oh! hard to understand. Did you replied only in text or image?");
				execute(message);
			} catch (TelegramApiException exception) {
				LOGGER.error(exception.getMessage(), exception);
			}
		}
	}

	private void introReply(long chatId) throws BotException {
		String smily = ":smiley:";
		SendMessage messageAnalysis = new SendMessage().setChatId(chatId)
				.setText("I am your psycotherapist bot. Ready to help you any time." + smily);
		try {
			execute(messageAnalysis);
		} catch (TelegramApiException e) {
			SendMessage message = new SendMessage().setChatId(chatId)
					.setText("Oh! something was not right. Please try again.");
			try {
				execute(message);
			} catch (TelegramApiException e1) {
				LOGGER.error(e.getMessage(), e);
				throw new BotException("Error while replying to text");
			}
		}
	}

	private void welcomeReply(long chatId) throws BotException {

		String smily = ":smiley:";
		SendMessage messageAnalysis = new SendMessage().setChatId(chatId).setText("Welcome to our bot! " + smily);
		try {
			execute(messageAnalysis);
		} catch (TelegramApiException e) {
			SendMessage message = new SendMessage().setChatId(chatId)
					.setText("Oh! something was not right. Please try again.");
			try {
				execute(message);
			} catch (TelegramApiException e1) {
				LOGGER.error(e.getMessage(), e);
				throw new BotException("Error while replying to text");
			}
		}
	}

	private boolean hasEmoji(Update update) {
		return !EmojiParser.extractEmojis(update.getMessage().getText()).isEmpty();
	}

	private void replyToEmoji(Update update, long chatId) throws BotException {
		SendMessage messageAnalysis = new SendMessage().setChatId(chatId).setText(
				"You sent an emoji. I cannot understand emojies rightnow but my developer is helping me understand them. I am a fast learner and will be read very soon.");
		try {
			execute(messageAnalysis);
		} catch (TelegramApiException e) {
			SendMessage message = new SendMessage().setChatId(chatId)
					.setText("Oh! something was not right. Please try again.");
			try {
				execute(message);
			} catch (TelegramApiException e1) {
				LOGGER.error(e.getMessage(), e);
				throw new BotException("Error while replying to text");
			}
		}
	}

	private void replyToPhoto(Update update, long chatId) throws BotException {
		try {
			GetFile getFileRequest = new GetFile();

			List<PhotoSize> photos = update.getMessage().getPhoto();
			String f_id = photos.stream().sorted(Comparator.comparing(PhotoSize::getFileSize).reversed()).findFirst()
					.orElse(null).getFileId();
			getFileRequest.setFileId(f_id);
			org.telegram.telegrambots.api.objects.File file = execute(getFileRequest);
			URL fileUrl = new URL(file.getFileUrl(getBotToken()));
			HttpURLConnection httpConn = (HttpURLConnection) fileUrl.openConnection();
			SendMessage message = new SendMessage().setChatId(chatId)
					.setText(AWSRekognitionBotUtil.textFromImage(httpConn.getInputStream()));
			execute(message);
			SendMessage messageAnalysis = new SendMessage().setChatId(chatId)
					.setText(AWSRekognitionBotUtil.textSentiments(message.getText()));
			execute(messageAnalysis);
		} catch (TelegramApiException | IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new BotException("Error while replying to photo");
		}
	}

	private void replyToText(Update update, long chatId) throws BotException {
		String message_text = update.getMessage().getText();

		SendMessage messageAnalysis = new SendMessage().setChatId(chatId)
				.setText(AWSRekognitionBotUtil.textSentiments(message_text));

		try {
			execute(messageAnalysis);
		} catch (TelegramApiException e) {

			SendMessage message = new SendMessage().setChatId(chatId)
					.setText("Oh! something was not right. Please try again.");
			try {
				execute(message);
			} catch (TelegramApiException e1) {
				LOGGER.error(e.getMessage(), e);
				throw new BotException("Error while replying to text");
			}
		}
	}
}
