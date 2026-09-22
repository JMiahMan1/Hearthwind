package mod.azure.logbegone;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.filter.AbstractFilter;

public final class JavaUtilLog4jFilter extends AbstractFilter implements Filter {
    public boolean isLoggable(LogRecord logRecord) {
        return !CommonMod.shouldFilterMessage(logRecord.getMessage());
    }

    @Override
    public Result filter(LogEvent event) {
        return CommonMod.shouldFilterMessage(
                "[" + event.getLoggerName() + "]: " + event.getMessage().getFormattedMessage()) ? Result.DENY
                : Result.NEUTRAL;
    }
}
