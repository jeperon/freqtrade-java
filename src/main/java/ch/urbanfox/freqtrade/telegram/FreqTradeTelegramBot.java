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
        final Message message = Optional.ofNullable(update.getMessage()).orElse(update.getEditedMessage());
        final Long chatId = message.getChatId();
        if (!telegramProperties.getChatId().equals(chatId)) {
            LOGGER.debug("Unauthorized access, ignoring (chat_id: {}, expected: {})", update.getMessage().getChatId(), telegramProperties.getChatId());
            return;
        }

        final String command = message.getText();
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
            final String unknownCommandMessage = String.format("Unkown command received: %s", command);
            LOGGER.info(unknownCommandMessage);
            SendMessage sendMessage = new SendMessage(telegramProperties.getChatId(), unknownCommandMessage);

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
