package ch.urbanfox.freqtrade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.telegram.telegrambots.exceptions.TelegramApiException;

@SpringBootApplication
public class FreqTradeApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(FreqTradeApplication.class);

    public static void main(String[] args) throws InterruptedException, TelegramApiException {
        LOGGER.info("Starting freqtrade...");

        ConfigurableApplicationContext context = SpringApplication.run(FreqTradeApplication.class, args);

        LOGGER.info("freqtrade started.");

        FreqTradeMainRunner runner = context.getBean(FreqTradeMainRunner.class);
        runner.main();
    }

}
