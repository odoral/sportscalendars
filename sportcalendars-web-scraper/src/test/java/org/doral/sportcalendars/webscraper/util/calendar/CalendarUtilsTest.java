package org.doral.sportcalendars.webscraper.util.calendar;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author odoral
 */
class CalendarUtilsTest {

    @Test
    public void getCalendarFileName() {
        assertEquals("bod__glimt.ics", CalendarUtils.getCalendarFileName("Bodø/Glimt"));
    }
}