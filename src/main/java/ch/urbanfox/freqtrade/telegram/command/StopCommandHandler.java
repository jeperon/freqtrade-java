package ch.urbanfox.freqtrade.telegram.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import ch.urbanfox.freqtrade.FreqTradeMainRunner;
import ch.urbanfox.freqtrade.telegram.TelegramService;
import ch.urbanfox.freqtrade.type.State;

/**
 * Handler for /stop
 */
@Component
public class StopCommandHandler extends AbstractCommandHandler {

    @Autowired
    private FreqTradeMainRunner runner;

    @Autowired
    private TelegramService telegramService;

    @Override
    public String getCommandName() {
        return "/stop";
    }

    /**
     * Stops trading
     *
     * @throws TelegramApiException if any error occurs while using Telegram API
     */
    @Override
    protected void handleInternal(String[] params) throws Exception {
        if (runner.getState() == State.RUNNING) {
            telegramService.sendMessage("`Stopping trader ...`");
            runner.updateState(State.STOPPED);
        } else {
            telegramService.sendMessage("*Status:* `already stopped`");
        }
    }

}
