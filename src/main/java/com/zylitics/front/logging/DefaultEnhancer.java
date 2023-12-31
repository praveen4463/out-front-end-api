package com.zylitics.front.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.google.cloud.logging.LogEntry;
import com.google.cloud.logging.logback.LoggingEventEnhancer;

public class DefaultEnhancer implements LoggingEventEnhancer {
  
  private static final String LABEL_REGEX = "[a-zA-Z0-9-_.]+";
  
  @Override
  public void enhanceLogEntry(LogEntry.Builder builder, ILoggingEvent iLoggingEvent) {
    if (iLoggingEvent.getThreadName() != null
        && iLoggingEvent.getThreadName().matches(LABEL_REGEX)) {
      builder.addLabel("thread", iLoggingEvent.getThreadName());
    }
  }
}
