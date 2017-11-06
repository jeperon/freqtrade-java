package ch.urbanfox.freqtrade.event.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class CommandEvent {

    private final String command;

    private final String[] params;

    public CommandEvent(String command, String... params) {
        this.command = command;
        this.params = params;
    }

    public String getCommand() {
        return command;
    }

    public String[] getParams() {
        return params;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
