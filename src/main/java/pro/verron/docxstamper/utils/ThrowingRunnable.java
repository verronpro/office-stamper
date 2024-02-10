package pro.verron.docxstamper.utils;


import org.wickedsource.docxstamper.api.DocxStamperException;

/**
 * A functional interface representing runnable task able to throw an exception.
 * It extends the {@link Runnable} interface and provides default implementation
 * of the {@link Runnable#run()} method handling the exception by rethrowing it
 * wrapped inside a {@link DocxStamperException}.
 */
public interface ThrowingRunnable
        extends Runnable {

    /**
     * Executes the runnable task, handling any exception by throwing it wrapped
     * inside a {@link DocxStamperException}.
     *
     * @throws DocxStamperException if an exception occurs executing the task
     */
    default void run() {
        try {
            throwingRun();
        } catch (Exception e) {
            throw new DocxStamperException(e);
        }
    }

    /**
     * Executes the runnable task
     *
     * @throws Exception if an exception occurs executing the task
     */
    void throwingRun() throws Exception;
}
