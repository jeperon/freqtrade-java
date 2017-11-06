package ch.urbanfox.freqtrade.telegram;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import ch.urbanfox.freqtrade.event.model.CommandEvent;

public class FreqTradeTelegramBot extends TelegramLongPollingBot {

    private static final Logger LOGGER = LoggerFactory.getLogger(FreqTradeTelegramBot.class);

    private final ApplicationContext context;

    private final TelegramProperties telegramProperties;

    public FreqTradeTelegramBot(ApplicationContext context, TelegramProperties telegramProperties) {
        this.context = context;
        this.telegramProperties = telegramProperties;
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
        context.publishEvent(new CommandEvent(command));
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
