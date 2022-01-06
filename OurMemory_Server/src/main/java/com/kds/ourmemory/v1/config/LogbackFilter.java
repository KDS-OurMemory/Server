package com.kds.ourmemory.v1.config;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

public class LogbackFilter extends Filter<ILoggingEvent> {

    @Override
    public FilterReply decide(ILoggingEvent event) {
        var filteringMsg = "Closing connections idle longer than 60000 MILLISECONDS";

        return event.getMessage().contains(filteringMsg)? FilterReply.DENY : FilterReply.ACCEPT;
    }

}
