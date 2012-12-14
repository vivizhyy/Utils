package vivi.utils.domain;

import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.PatternLayout;

public class LogFileAppender extends DailyRollingFileAppender {
    public LogFileAppender() {
        this.encoding = "UTF-8";
        this.layout = new PatternLayout("%d %c:%L [%5p]: %m%n");
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }

}
