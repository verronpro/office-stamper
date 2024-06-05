package pro.verron.officestamper.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.ExpressionException;
import pro.verron.officestamper.api.OfficeStamperException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ExceptionUtil {

    private static final Logger logger;
    private static final Map<Boolean, Map<Boolean, BiConsumer<Exception, String>>> EXCEPTION_HANDLERS;
    private static final BiConsumer<Exception, String> stacktraceLogger;
    private static final Consumer<String> simpleLogger;

    static {
        logger = LoggerFactory.getLogger(ExceptionUtil.class);
        stacktraceLogger = (Exception e, String msg) -> logger.warn(msg, e);
        simpleLogger = logger::warn;

        EXCEPTION_HANDLERS = new HashMap<>();
        var verboseHandlers = new HashMap<Boolean, BiConsumer<Exception, String>>();
        verboseHandlers.put(true, ExceptionUtil::throwStacktrace); // TRACE ON - THROW WITH STACKTRACE
        verboseHandlers.put(false, ExceptionUtil::logStacktrace); // TRACE ON - LOG WITH STACKTRACE
        EXCEPTION_HANDLERS.put(true, verboseHandlers);
        var quietHandlers = new HashMap<Boolean, BiConsumer<Exception, String>>();
        quietHandlers.put(true, ExceptionUtil::throwSimple); // TRACE OFF - THROW W/O STACKTRACE
        quietHandlers.put(false, ExceptionUtil::logSimple); // TRACE OFF - LOG W/O STACKTRACE
        EXCEPTION_HANDLERS.put(false, quietHandlers);

    }

    static void treatException(ExpressionException exception, boolean shouldThrow, String msg) {
        treatException(exception, shouldThrow, msg, () -> "Do not expect a return");
    }

    static <T> T treatException(
            ExpressionException exception, boolean shouldThrow, String msg,
            Supplier<T> supplier
    ) {
        EXCEPTION_HANDLERS
                .get(logger.isTraceEnabled())
                .get(shouldThrow)
                .accept(exception, msg);
        if (!logger.isTraceEnabled())
            simpleLogger.accept("Set log level to TRACE to view Stacktrace.");
        return supplier.get();
    }

    private static void throwStacktrace(Exception e, String msg) {
        throw new OfficeStamperException(msg, e);
    }

    private static void logStacktrace(Exception e, String msg) {
        stacktraceLogger.accept(e, msg);
    }

    private static void throwSimple(Exception e, String msg) {
        throw new OfficeStamperException(msg);
    }

    private static void logSimple(Exception e, String msg) {
        simpleLogger.accept(msg);
    }
}
