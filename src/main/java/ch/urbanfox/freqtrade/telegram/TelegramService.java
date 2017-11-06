package ch.urbanfox.freqtrade.telegram;

import org.telegram.telegrambots.exceptions.TelegramApiException;

/**
 * Telegram messaging service
 */
public interface TelegramService {

    /**
     * Sends a message to the user of the application
     *
     * @param message the content of the message
     * @throws TelegramApiException if any error occurs while using Telegram API
     */
    void sendMessage(String message) throws TelegramApiException;

    void sendMessage(String message, String parseMode) throws TelegramApiException;

}
