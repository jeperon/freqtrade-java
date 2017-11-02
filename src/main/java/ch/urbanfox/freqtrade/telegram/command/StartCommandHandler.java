package ch.urbanfox.freqtrade.telegram.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import ch.urbanfox.freqtrade.FreqTradeMainRunner;
import ch.urbanfox.freqtrade.telegram.TelegramService;
import ch.urbanfox.freqtrade.type.State;

/**
 * Handler for /start
 */
@Component
public class StartCommandHandler {

    @Autowired
    private FreqTradeMainRunner runner;

    @Autowired
    private TelegramService telegramService;

    public void start() throws TelegramApiException {
        if (runner.getState() == State.RUNNING) {
            telegramService.sendMessage("*Status:* `already running`");
        } else {
            runner.updateState(State.RUNNING);
        }
    }

}
