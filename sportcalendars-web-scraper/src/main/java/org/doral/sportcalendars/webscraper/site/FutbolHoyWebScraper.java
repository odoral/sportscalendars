package org.doral.sportcalendars.webscraper.site;

import org.apache.commons.lang3.StringUtils;
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
public class FutbolHoyWebScraper extends AbstractHoySiteWebScraper {

    private static final Logger LOGGER = LoggerFactory.getLogger(FutbolHoyWebScraper.class);

    public static final String BASE_URL = "https://www.futbolhoy.es/";

    @Override
    public List<Calendar> parseCalendars() throws SiteWebScraperException {
        LOGGER.info("Parsing football events");
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
            HtmlPage page = webClient.getPage(FutbolHoyWebScraper.BASE_URL);
            return page.getByXPath("//div[@id='daysCarousel']/time")
                    .stream()
                    .filter(HtmlElement.class::isInstance)
                    .map(HtmlElement.class::cast)
                    .filter(htmlElement -> StringUtils.isNotBlank(htmlElement.getAttribute("data-target")))
                    .map(htmlElement -> htmlElement.getAttribute("data-target"))
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
