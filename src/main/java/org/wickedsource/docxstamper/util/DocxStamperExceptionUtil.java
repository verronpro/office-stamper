package org.wickedsource.docxstamper.util;

import org.wickedsource.docxstamper.api.DocxStamperException;

/**
 * Convenience methods for wrapping checked exceptions into DocxStamperException in a single line.
 * @author jenei.attila
 */
public class DocxStamperExceptionUtil{
    
    @FunctionalInterface
    public interface RunnableThrowingException{
        @SuppressWarnings("java:S112") //sonar take it easy
        void run() throws Exception;
    }
    
    public static void docxStamperExceptionOf(RunnableThrowingException runnable) throws DocxStamperException{
        try{
            runnable.run();
        }
        catch(DocxStamperException e){
            throw e;
        }
        catch(Exception e){
            throw new DocxStamperException(e);
        }
    }

    public static DocxStamperException docxStamperExceptionOf(Throwable e){
        return e instanceof DocxStamperException dse ? dse : new DocxStamperException(e);
    }
}
