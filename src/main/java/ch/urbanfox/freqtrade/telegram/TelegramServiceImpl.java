package ch.urbanfox.freqtrade.telegram;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.ParseMode;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;

import ch.urbanfox.freqtrade.FreqTradeProperties;

/**
 * Default implementation of the telegram service
 */
@Service
public class TelegramServiceImpl implements TelegramService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramServiceImpl.class);

    private final ApplicationContext context;

    private final TelegramProperties telegramProperties;

    private FreqTradeTelegramBot bot;

    @Autowired
    public TelegramServiceImpl(ApplicationContext context, FreqTradeProperties properties) {
        this.context = context;
        this.telegramProperties = properties.getTelegram();
    }

    @Override
    public void sendMessage(String message) throws TelegramApiException {
        sendMessage(message, ParseMode.MARKDOWN);
    }

    @Override
    public void sendMessage(String message, String parseMode) throws TelegramApiException {
        if (telegramProperties.getEnabled()) {
            LOGGER.info("Sending message: {}", message);

            SendMessage sendMessage = new SendMessage(telegramProperties.getChatId(), message);
            sendMessage.setParseMode(parseMode);

            bot.execute(sendMessage);
        } else {
            LOGGER.info("Telegram is disabled, message: '{}' not sent", message);
        }
    }

    @PostConstruct
    public void init() throws TelegramApiRequestException {
        if (telegramProperties.getEnabled()) {
            LOGGER.info("Initializing Telegram service...");

            ApiContextInitializer.init();

            TelegramBotsApi botsApi = new TelegramBotsApi();

            bot = new FreqTradeTelegramBot(context, telegramProperties);

            botsApi.registerBot(bot);
        } else {
            LOGGER.info("Telegram service is disabled");
        }
    }

}
