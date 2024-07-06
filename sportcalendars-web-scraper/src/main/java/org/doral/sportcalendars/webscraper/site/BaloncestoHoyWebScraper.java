package org.doral.sportcalendars.webscraper.site;

import org.doral.sportcalendars.webscraper.model.calendar.Calendar;
import org.doral.sportcalendars.webscraper.site.exception.SiteWebScraperException;
import org.htmlunit.BrowserVersion;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author odoral
 */
public class BaloncestoHoyWebScraper extends AbstractHoySiteWebScraper {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaloncestoHoyWebScraper.class);

    public static final String BASE_URL = "https://www.baloncestohoy.es";

    @Override
    public List<Calendar> parseCalendars() throws SiteWebScraperException {
        LOGGER.info("Parsing basket events");
        return new ArrayList<>(parseCarousel().stream()
                .parallel()
                .map(target -> parsePage(String.join("", BASE_URL, target)))
                .flatMap(calendarMap -> calendarMap.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Calendar::merge)).values());
    }

    protected List<String> parseCarousel() {
        LOGGER.info("Parsing carousel");
        try (WebClient webClient = new WebClient(BrowserVersion.BEST_SUPPORTED)) {
            webClient.getOptions().setThrowExceptionOnScriptError(false);
            HtmlPage page = webClient.getPage(BaloncestoHoyWebScraper.BASE_URL);
            return page.getByXPath("//div[@id='daysCarousel']/a")
                    .stream()
                    .filter(HtmlElement.class::isInstance)
                    .map(HtmlElement.class::cast)
                    .map(htmlElement -> htmlElement.getAttribute("href"))
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected Map<String, Calendar> parsePage(String pageURL) {
        try {
            return parsePage(new URI(pageURL).toURL());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
