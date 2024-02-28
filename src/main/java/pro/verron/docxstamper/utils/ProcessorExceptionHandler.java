package pro.verron.docxstamper.utils;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class is responsible for capturing and handling uncaught exceptions
 * that occur in a thread.
 * It implements the {@link Thread.UncaughtExceptionHandler} interface and can
 * be assigned to a thread using the
 * {@link Thread#setUncaughtExceptionHandler(Thread.UncaughtExceptionHandler)} method.
 * When an exception occurs in the thread,
 * the {@link ProcessorExceptionHandler#uncaughtException(Thread, Throwable)}
 * method will be called.
 * This class provides the following features:
 * 1. Capturing and storing the uncaught exception.
 * 2. Executing a list of routines when an exception occurs.
 * 3. Providing access to the captured exception, if any.
 * Example usage:
 * <code>
 * ProcessorExceptionHandler exceptionHandler = new
 * ProcessorExceptionHandler(){};
 * thread.setUncaughtExceptionHandler(exceptionHandler);
 * </code>
 *
 * @see Thread.UncaughtExceptionHandler
 * @author Joseph Verron
 * @version ${version}
 */
public class ProcessorExceptionHandler
        implements Thread.UncaughtExceptionHandler {
    private final AtomicReference<Throwable> exception;
    private final List<Runnable> onException;

    /**
     * Constructs a new instance for managing thread's uncaught exceptions.
     * Once set to a thread, it retains the exception information and performs specified routines.
     */
    public ProcessorExceptionHandler() {
        this.exception = new AtomicReference<>();
        this.onException = new CopyOnWriteArrayList<>();
    }

    /**
     * {@inheritDoc}
     *
     * Captures and stores an uncaught exception from a thread run
     * and executes all defined routines on occurrence of the exception.
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
     * @return an {@link Optional} containing the captured exception,
     * or an {@link Optional#empty()} if no exception was captured
     */
    public Optional<Throwable> exception() {
        return Optional.ofNullable(exception.get());
    }
}
