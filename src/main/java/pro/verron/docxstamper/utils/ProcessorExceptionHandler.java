package pro.verron.docxstamper.utils;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The ProcessorExceptionHandler class is responsible for capturing and handling uncaught exceptions that occur in a thread.
 * It implements the Thread.UncaughtExceptionHandler interface and can be assigned to a thread using the setUncaughtExceptionHandler() method.
 * When an exception occurs in the thread, the uncaughtException() method of ProcessorExceptionHandler will be called.
 * This class provides the following features:
 * 1. Capturing and storing the uncaught exception.
 * 2. Executing a list of routines when an exception occurs.
 * 3. Providing access to the captured exception, if any.
 * Example usage:
 * ProcessorExceptionHandler exceptionHandler = new ProcessorExceptionHandler();
 * thread.setUncaughtExceptionHandler(exceptionHandler);
 *
 * @see Thread.UncaughtExceptionHandler
 * @author Joseph Verron
 * @version 1.6.6
 */
public class ProcessorExceptionHandler
        implements Thread.UncaughtExceptionHandler {
    private final AtomicReference<Throwable> exception;
    private final List<Runnable> onException;

    /**
     * Constructs a new ProcessorExceptionHandler for managing thread's uncaught exceptions.
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
     * @return an Optional containing the captured exception,
     * or an empty Optional if no exception was captured
     */
    public Optional<Throwable> exception() {
        return Optional.ofNullable(exception.get());
    }
}
