package ch.urbanfox.freqtrade;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import ch.urbanfox.freqtrade.telegram.TelegramProperties;

/**
 * FreqTrade configuration properties
 */
@Configuration
@ConfigurationProperties(prefix = "freqtrade")
public class FreqTradeProperties {

    private TelegramProperties telegram;

    public TelegramProperties getTelegram() {
        return telegram;
    }

    public void setTelegram(TelegramProperties telegram) {
        this.telegram = telegram;
    }

}
