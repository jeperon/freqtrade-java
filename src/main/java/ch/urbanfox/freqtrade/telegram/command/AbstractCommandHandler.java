package ch.urbanfox.freqtrade.telegram.command;

import org.springframework.context.event.EventListener;

import ch.urbanfox.freqtrade.event.model.CommandEvent;
import jersey.repackaged.com.google.common.base.Objects;

public abstract class AbstractCommandHandler implements CommandHandler {

    @EventListener
    public void eventListener(CommandEvent event) throws Exception {
        if (Objects.equal(getCommandName(), event.getCommand())) {
            handleInternal(event.getParams());
        }
    }

    protected abstract void handleInternal(String[] params) throws Exception;

}
