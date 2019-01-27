package com.telegram.bot.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

import org.apache.log4j.Logger;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.comprehend.AmazonComprehend;
import com.amazonaws.services.comprehend.AmazonComprehendClientBuilder;
import com.amazonaws.services.comprehend.model.DetectSentimentRequest;
import com.amazonaws.services.comprehend.model.DetectSentimentResult;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.AmazonRekognitionException;
import com.amazonaws.services.rekognition.model.DetectTextRequest;
import com.amazonaws.services.rekognition.model.DetectTextResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.TextDetection;
import com.amazonaws.util.IOUtils;
import com.telegram.bot.constants.BotConstants;
import com.telegram.bot.exception.BotException;

public class AWSRekognitionBotUtil {

	private static final Logger LOGGER = Logger.getLogger(AWSRekognitionBotUtil.class.getName());

	public static String textFromImage(InputStream inputStream) throws IOException, BotException {

		BasicAWSCredentials credential;
		StringBuilder strBuilder = new StringBuilder();
		try {
			credential = new BasicAWSCredentials(Utilities.getProperty(BotConstants.AWS_ACCESS_KEY),
					Utilities.getProperty(BotConstants.AWS_SECRET_KEY));
		} catch (AmazonClientException e) {
			LOGGER.error(e.getMessage());
			throw new AmazonClientException("Invalid AWS credentials");
		}

		ByteBuffer imageBytes = null;
		try {
			imageBytes = java.nio.ByteBuffer.wrap(IOUtils.toByteArray(inputStream));
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			throw new AmazonClientException("Error while reading photo");
		} finally {
			inputStream.close();
		}
		AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.standard().withRegion(Regions.US_EAST_1)
				.withCredentials(new AWSStaticCredentialsProvider(credential)).build();
		DetectTextRequest request = new DetectTextRequest().withImage(new Image().withBytes(imageBytes));
		try {
			DetectTextResult result = rekognitionClient.detectText(request);
			List<TextDetection> textDetections = result.getTextDetections();

			for (TextDetection text : textDetections) {

				if ("LINE".equals(text.getType())) {
					strBuilder.append(text.getDetectedText()).append("\n");
				}
			}
		} catch (AmazonRekognitionException e) {
			LOGGER.error(e.getMessage());
			throw new AmazonClientException("Error while recognizing sentiment for photo.");
		}
		return replyToSentiment(strBuilder.toString());
	}

	public static String textSentiments(String textToComprehend) throws BotException {

		BasicAWSCredentials credential;
		try {
			credential = new BasicAWSCredentials(Utilities.getProperty(BotConstants.AWS_ACCESS_KEY),
					Utilities.getProperty(BotConstants.AWS_SECRET_KEY));
		} catch (AmazonClientException e) {
			LOGGER.error(e.getMessage());
			throw new AmazonClientException("Invalid AWS credentials");
		}

		StringBuilder message = new StringBuilder();

		AmazonComprehend comprehendClient = AmazonComprehendClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(credential)).withRegion(Regions.US_EAST_1).build();

		DetectSentimentRequest detectSentimentRequest = new DetectSentimentRequest().withText(textToComprehend)
				.withLanguageCode("en");
		DetectSentimentResult detectSentimentResult = comprehendClient.detectSentiment(detectSentimentRequest);
		message.append(detectSentimentResult.getSentiment());
		return replyToSentiment(message.toString());
	}

	private static String replyToSentiment(String sentiment) {

		StringBuilder message = new StringBuilder();
		if (sentiment.equalsIgnoreCase("POSITIVE")) {
			message.append("That's good. ");
		} else if (sentiment.equalsIgnoreCase("NEGATIVE")) {
			message.append("Ohh! ");
		} else if (sentiment.equalsIgnoreCase("NEUTRAL")) {
			message.append("Right. What else you want to talk about?");
		} else {
			message.append("Hmm! hard to understand. Can you explain a bit?");
		}
		return message.toString();
	}
}
