package ch.urbanfox.freqtrade.telegram;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import ch.urbanfox.freqtrade.FreqTradeProperties;

public class FreqTradeTelegramBot extends TelegramLongPollingBot {

    private static final Logger LOGGER = LoggerFactory.getLogger(FreqTradeTelegramBot.class);

    private final TelegramProperties telegramProperties;

    public FreqTradeTelegramBot(FreqTradeProperties properties) {
        this.telegramProperties = properties.getTelegram();
    }

    @Override
    public void onUpdateReceived(Update update) {
        LOGGER.debug("Received: {}", update);
        final Long messageChatId = Optional.ofNullable(update.getMessage()).map(Message::getChatId).orElse(null);
        final Long editedMessageChatId = Optional.ofNullable(update.getEditedMessage()).map(Message::getChatId).orElse(null);
        final Long chatId = Optional.ofNullable(messageChatId).orElse(editedMessageChatId);
        if (!telegramProperties.getChatId().equals(chatId)) {
            LOGGER.debug("Unauthorized access, ignoring (chat_id: {}, expected: {})", update.getMessage().getChatId(), telegramProperties.getChatId());
            return;
        }

        final String command = update.getMessage().getText();
        switch (command) {
        case "/profit":
            LOGGER.info("/profit command");
            // TODO implements the response to the profit command
            break;

        case "/performance":
            LOGGER.info("/performance command");
            // TODO implements the response to the performance command
            break;

        case "/status":
            LOGGER.info("/status command");
            // TODO implements the response to the status command
            break;

        default:
            final String message = String.format("Unkown command received: %s", command);
            LOGGER.info(message);
            SendMessage sendMessage = new SendMessage(telegramProperties.getChatId(), message);

            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                LOGGER.warn("Unable to reply to message", e);
            }
        }

    }

    @Override
    public String getBotUsername() {
        return telegramProperties.getBotName();
    }

    @Override
    public String getBotToken() {
        return telegramProperties.getToken();
    }

}
