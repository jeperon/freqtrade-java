package ch.urbanfox.freqtrade.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import ch.urbanfox.freqtrade.event.model.CommandEvent;
import ch.urbanfox.freqtrade.telegram.TelegramService;

@Component
public class CommandEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandEventListener.class);

    private final TelegramService telegramService;

    @Autowired
    public CommandEventListener(TelegramService telegramService) {
        this.telegramService = telegramService;
    }

    @EventListener
    public void onCommandEvent(CommandEvent event) {
        LOGGER.debug("Received event: {}", event);

        final String command = event.getCommand();
        switch (command) {
        case "/performance":
            LOGGER.info("/performance command");
            // TODO implements the response to the performance command
            break;

        case "/profit":
            LOGGER.info("/profit command");
            // TODO implements the response to the profit command
            break;

        case "/status":
            LOGGER.info("/status command");
            // TODO implements the response to the status command
            break;

        default:
            final String unknownCommandMessage = String.format("Unkown command received: %s", command);
            LOGGER.info(unknownCommandMessage);

            try {
                telegramService.sendMessage(unknownCommandMessage);
            } catch (TelegramApiException e) {
                LOGGER.warn("Unable to reply to message", e);
            }
        }

    }

}
