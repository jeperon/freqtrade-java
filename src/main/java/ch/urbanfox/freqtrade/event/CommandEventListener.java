package ch.urbanfox.freqtrade.event;

import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import ch.urbanfox.freqtrade.event.model.CommandEvent;
import ch.urbanfox.freqtrade.telegram.TelegramService;
import ch.urbanfox.freqtrade.telegram.command.CommandHandler;

@Component
public class CommandEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandEventListener.class);

    private final TelegramService telegramService;

    private final Set<String> availableCommandNames;

    @Autowired
    public CommandEventListener(TelegramService telegramService, Set<CommandHandler> handlers) {
        this.telegramService = telegramService;
        this.availableCommandNames = handlers.stream()
                .map(CommandHandler::getCommandName)
                .collect(Collectors.toSet());
    }

    @EventListener
    public void onCommandEvent(CommandEvent event) {
        LOGGER.debug("Received event: {}", event);

        final String command = event.getCommand();
        if (!availableCommandNames.contains(command)) {

            final String unknownCommandMessage = String.format("Unkown command received: %s", command);
            LOGGER.debug(unknownCommandMessage);

            try {
                telegramService.sendMessage(unknownCommandMessage);
                telegramService.sendMessage(String.format("Available commands: %s", availableCommandNames));
            } catch (TelegramApiException e) {
                LOGGER.warn("Unable to reply to message", e);
            }
        }

    }

}
