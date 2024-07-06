package org.doral.sportcalendars.webscraper.writer;

import org.doral.sportcalendars.webscraper.model.calendar.Calendar;
import org.doral.sportcalendars.webscraper.writer.exception.CalendarWriterException;

import java.io.OutputStream;

/**
 * @author odoral
 */
public interface ICalendarWriter {
    void write(OutputStream outputStream, String prodId, Calendar... calendar) throws CalendarWriterException;
}
