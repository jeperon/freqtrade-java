package ch.urbanfox.freqtrade.telegram;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import ch.urbanfox.freqtrade.FreqTradeApplication;

/**
 * Simple application to test you telegram configuration
 */
public class TelegramServiceTester {

    public static void main(String[] args) throws Exception {
        ApplicationContext context = SpringApplication.run(FreqTradeApplication.class, args);

        TelegramService telegramService = context.getBean(TelegramService.class);

        telegramService.sendMessage("Hi ! This is a test");
    }

}
