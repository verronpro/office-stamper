package pro.verron.docxstamper.utils;


import org.wickedsource.docxstamper.api.DocxStamperException;

/**
 * A functional interface representing runnable task able to throw an exception.
 * It extends the {@link java.lang.Runnable} interface and provides default implementation
 * of the {@link java.lang.Runnable#run()} method handling the exception by rethrowing it
 * wrapped inside a {@link org.wickedsource.docxstamper.api.DocxStamperException}.
 *
 * @author Joseph Verron
 * @version ${version}
 */
public interface ThrowingRunnable
        extends Runnable {

    /**
     * Executes the runnable task, handling any exception by throwing it wrapped
     * inside a {@link org.wickedsource.docxstamper.api.DocxStamperException}.
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
     * @throws java.lang.Exception if an exception occurs executing the task
     */
    void throwingRun() throws Exception;
}
