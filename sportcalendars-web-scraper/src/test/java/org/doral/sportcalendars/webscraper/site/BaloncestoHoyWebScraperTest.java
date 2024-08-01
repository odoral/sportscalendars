package org.doral.sportcalendars.webscraper.site;

import org.doral.sportcalendars.webscraper.model.calendar.Calendar;
import org.htmlunit.html.HtmlElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author odoral
 */
class BaloncestoHoyWebScraperTest {

    public static final String AMISTOSO = "Amistoso";
    public static final String AMISTOSO_FEMENINO = "Amistoso Femenino";
    public static final String NBA = "NBA";
    protected URL url;
    protected BaloncestoHoyWebScraper scraper;

    @BeforeEach
    void setUp() {
        url = getClass().getResource("/baloncestohoy/index.html");
        assert url != null;
        scraper = new BaloncestoHoyWebScraper();
    }

    @Test
    public void test() {
        Map<String, Calendar> result = scraper.parsePage(url);

        assertNotNull(result);
        assertTrue(result.containsKey(AMISTOSO));
        assertEquals(2, result.get(AMISTOSO).getEvents().size());

        assertTrue(result.containsKey(AMISTOSO_FEMENINO));
        assertEquals(2, result.get(AMISTOSO_FEMENINO).getEvents().size());

        assertTrue(result.containsKey(NBA));
        assertEquals(1, result.get(NBA).getEvents().size());
    }

    @Test
    public void testParseEventDate() {
        Locale defaultLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.US);

            HtmlElement dateElement = mock(HtmlElement.class);
            when(dateElement.getTextContent()).thenReturn("1 de agosto");
            Date date = scraper.parseEventDate(dateElement);
            assertEquals("0801", new SimpleDateFormat("MMdd").format(date));

            when(dateElement.getTextContent()).thenReturn("Hoy jueves, 1 de agosto (5)");
            date = scraper.parseEventDate(dateElement);
            assertEquals("0801", new SimpleDateFormat("MMdd").format(date));
        } finally {
            Locale.setDefault(defaultLocale);
        }
    }
}