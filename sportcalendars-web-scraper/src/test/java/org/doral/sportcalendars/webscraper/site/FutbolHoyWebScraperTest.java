package org.doral.sportcalendars.webscraper.site;

import org.doral.sportcalendars.webscraper.model.calendar.Calendar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author odoral
 */
class FutbolHoyWebScraperTest {

    public static final String EUROCOPA_2024 = "Eurocopa 2024";
    public static final String COPA_AMERICA = "Copa América";
    protected URL url;
    protected FutbolHoyWebScraper scraper;

    @BeforeEach
    void setUp() {
        url = getClass().getResource("/futbolhoy/index.html");
        assert url != null;
        scraper = new FutbolHoyWebScraper();
    }

    @Test
    public void test() {
        Map<String, Calendar> result = scraper.parsePage(url);

        assertNotNull(result);
        assertTrue(result.containsKey(EUROCOPA_2024));
        assertEquals(14, result.get(EUROCOPA_2024).getEvents().size());
        result.get(EUROCOPA_2024).getEvents().forEach(event -> {
            assertNotNull(event.getChannels());
            assertNotNull(event.getName());
            assertNotNull(event.getStartTimestamp());
        });
        assertTrue(result.containsKey(COPA_AMERICA));
        assertEquals(1, result.get(COPA_AMERICA).getEvents().size());
        result.get(COPA_AMERICA).getEvents().forEach(event -> {
            assertNotNull(event.getChannels());
            assertNotNull(event.getName());
            assertNotNull(event.getStartTimestamp());
        });
    }
}