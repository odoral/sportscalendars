package org.doral.sportcalendars.webscraper.runner.exception;

/**
 * @author odoral
 */
public class WebScraperAppException extends RuntimeException {
    public WebScraperAppException() {
    }

    public WebScraperAppException(String message) {
        super(message);
    }

    public WebScraperAppException(String message, Throwable cause) {
        super(message, cause);
    }

    public WebScraperAppException(Throwable cause) {
        super(cause);
    }

    public WebScraperAppException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
