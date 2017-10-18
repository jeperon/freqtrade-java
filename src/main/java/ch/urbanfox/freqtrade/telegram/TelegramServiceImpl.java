package ch.urbanfox.freqtrade.telegram;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
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

    private final FreqTradeProperties properties;

    private FreqTradeTelegramBot bot;

    @Autowired
    public TelegramServiceImpl(FreqTradeProperties properties) {
        this.properties = properties;
    }

    @Override
    public void sendMessage(String message) throws TelegramApiException {
        LOGGER.info("Sending message: {}", message);

        SendMessage sendMessage = new SendMessage(properties.getTelegram().getChatId(), message);

        bot.execute(sendMessage);
    }

    @PostConstruct
    public void init() throws TelegramApiRequestException {
        LOGGER.info("Initializing Telegram service...");

        if (properties.getTelegram().getEnabled()) {
            ApiContextInitializer.init();

            TelegramBotsApi botsApi = new TelegramBotsApi();

            bot = new FreqTradeTelegramBot(properties);

            botsApi.registerBot(bot);
        }
    }

}
