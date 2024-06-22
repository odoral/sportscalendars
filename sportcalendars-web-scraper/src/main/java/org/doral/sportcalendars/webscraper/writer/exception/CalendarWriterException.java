package org.doral.sportcalendars.webscraper.writer.exception;

/**
 * @author odoral
 */
public class CalendarWriterException extends Exception {
    public CalendarWriterException() {
    }

    public CalendarWriterException(String message) {
        super(message);
    }

    public CalendarWriterException(String message, Throwable cause) {
        super(message, cause);
    }

    public CalendarWriterException(Throwable cause) {
        super(cause);
    }

    public CalendarWriterException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
