package com.telegram.bot.exception;

public class BotException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BotException(String msg, Throwable e) {
		super(msg, e);
	}

	public BotException(String msg) {
		super(msg);
	}
}
