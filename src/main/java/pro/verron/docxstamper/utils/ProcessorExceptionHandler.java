package pro.verron.docxstamper.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class acts as an exception handler for multithreading execution,
 * specifically for uncaught exceptions.
 * It stores the exception and performs predefined routines
 * when an exception occurs.
 *
 * <ul><p>Methods :</p>
 * <li>uncaughtException() : Captures and stores an uncaught exception
 * from a thread run and executes all defined routines on occurrence
 * of the exception.</li>
 * <li>onException() : Adds a routine to the list of routines that should be run
 * when an exception occurs.</li>
 * <li>exception() : Returns the captured exception if present.</li>
 * </ul>
 */
public class ProcessorExceptionHandler
        implements Thread.UncaughtExceptionHandler {
    private final AtomicReference<Throwable> exception;
    private final List<Runnable> onException;

    public ProcessorExceptionHandler() {
        this.exception = new AtomicReference<>();
        onException = new ArrayList<>();
    }

    /**
     * Captures and stores an uncaught exception from a thread run
     * and executes all defined routines on occurrence of the exception.
     *
     * @param t the thread where the exception occurred
     * @param e the uncaught exception
     */
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        exception.set(e);
        onException.forEach(Runnable::run);
    }

    /**
     * Adds a routine to the list of routines that should be run
     * when an exception occurs.
     *
     * @param runnable The runnable routine to be added
     */
    public void onException(ThrowingRunnable runnable) {
        onException.add(runnable);
    }

    /**
     * Returns the captured exception if present.
     *
     * @return an Optional containing the captured exception,
     * or an empty Optional if no exception was captured
     */
    public Optional<Throwable> exception() {
        return Optional.ofNullable(exception.get());
    }
}
