package ch.urbanfox.freqtrade.event.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class CommandEvent {

    private final String command;

    public CommandEvent(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
