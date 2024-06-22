package org.doral.sportcalendars.webscraper.site;

import org.apache.commons.collections4.KeyValue;
import org.apache.commons.collections4.keyvalue.DefaultKeyValue;
import org.apache.commons.lang3.time.CalendarUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.doral.sportcalendars.webscraper.model.Calendar;
import org.doral.sportcalendars.webscraper.model.Channel;
import org.doral.sportcalendars.webscraper.model.SportEvent;
import org.doral.sportcalendars.webscraper.util.htmlunit.HTMLUnitUtils;
import org.htmlunit.BrowserVersion;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlDivision;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author odoral
 */
public abstract class AbstractHoySiteWebScraper implements ISiteWebScraper {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractHoySiteWebScraper.class);

    protected Map<String, Calendar> parsePage(URL pageURL) {
        LOGGER.info("Parsing {}", pageURL);
        Map<String, Calendar> calendarsByName;
        try (WebClient webClient = new WebClient(BrowserVersion.BEST_SUPPORTED)) {
            webClient.getOptions().setThrowExceptionOnScriptError(false);
            HtmlPage page = webClient.getPage(pageURL);

            calendarsByName = lookForMatchDays(page).stream()
                    .filter(HtmlDivision.class::isInstance)
                    .map(HtmlDivision.class::cast)
                    .map(this::parseDay)
                    .flatMap(List::stream)
                    .collect(Collectors.toMap(Calendar::getName, Function.identity(), Calendar::merge));

            LOGGER.info("Parsed: {}", calendarsByName.keySet());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return calendarsByName;
    }

    protected List<Object> lookForMatchDays(HtmlPage page) {
        return page.getByXPath("//div[@class='matchday']");
    }

    protected List<Calendar> parseDay(HtmlDivision htmlDivision) {
        try {
            Date eventDate = null;
            Calendar calendar = null;
            List<Calendar> calendars = new ArrayList<>();
            for (HtmlElement descendant : htmlDivision.getHtmlElementDescendants()) {
                if (descendant.getAttribute("class").contains("matchdayHeader")) {
                    eventDate = parseEventDate(descendant, eventDate);
                } else if (HTMLUnitUtils.attributeContainsValue(descendant, "class", "matchdayCompetitionHeader")) {
                    calendar = Calendar.builder()
                            .name(HTMLUnitUtils.extractTextByFirstXpath(descendant, "./h3").orElseThrow())
                            .events(new ArrayList<>())
                            .build();
                    calendars.add(calendar);
                } else if (HTMLUnitUtils.attributeContainsValue(descendant, "class", "match")) {
                    assert calendar != null;
                    calendar.getEvents()
                            .add(parseEvent(calendar.getName(), eventDate, descendant));
                }
            }

            return calendars;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected SportEvent parseEvent(String calendarName, Date eventDate, HtmlElement htmlElement) {
        KeyValue<Date, String> startTimestampAndDisclaimer = parseEventTimeStamp(eventDate, htmlElement);
        SportEvent sportEvent = SportEvent.builder()
                .startTimestamp(startTimestampAndDisclaimer.getKey())
                .disclaimer(startTimestampAndDisclaimer.getValue())
                .name(String.join(" - ", calendarName, parseEventName(htmlElement)))
                .channels(parseEventChannels(htmlElement))
                .build();
        assert sportEvent.getName() != null;
        assert sportEvent.getStartTimestamp() != null;
        return sportEvent;
    }

    protected List<Channel> parseEventChannels(HtmlElement htmlElement) {
        return htmlElement.getByXPath("./div[@class='m_chan']/span")
                .stream()
                .filter(HtmlElement.class::isInstance)
                .map(HtmlElement.class::cast)
                .map(he -> Channel.builder()
                        .name(he.getTextContent())
                        .type(HTMLUnitUtils.attributeContainsValue(he, "class", "chFree") ? Channel.Type.OPEN : Channel.Type.PAY_PER_VIEW)
                        .build())
                .toList();
    }

    protected String parseEventName(HtmlElement htmlElement) {
        return HTMLUnitUtils.extractTextByFirstXpath(htmlElement, ".//div[contains(@class, 'm_title')]").orElseThrow();
    }

    protected KeyValue<Date, String> parseEventTimeStamp(Date eventDate, HtmlElement htmlElement) {
        String disclaimer = null;
        Optional<LocalTime> localTime = HTMLUnitUtils.extractTextByFirstXpath(htmlElement, ".//span[@class='m_time']")
                .map(AbstractHoySiteWebScraper::getLocalTime)
                .filter(Optional::isPresent)
                .map(Optional::get);

        if (localTime.isPresent()) {
            eventDate = DateUtils.setMinutes(eventDate, localTime.get().getMinute());
            eventDate = DateUtils.setHours(eventDate, localTime.get().getHour());
        } else {
            disclaimer = HTMLUnitUtils.extractTextByFirstXpath(htmlElement, ".//span[@class='m_time_pending']")
                    .orElse("Pendiente de confirmar");
        }

        return new DefaultKeyValue<>(eventDate, disclaimer);
    }

    public static Optional<LocalTime> getLocalTime(String timeText) {
        try {
            return Optional.of(LocalTime.from(DateTimeFormatter.ofPattern("HH:mm").parse(timeText)));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    protected Date parseEventDate(HtmlElement dateElement, Date eventDate) {
        String datePattern = "dd 'de' MMMM";
        String textContent = dateElement.getTextContent();
        String[] fields = textContent.split("\\s+");
        int deIndex = Arrays.binarySearch(fields, "de");
        textContent = String.join(" ", fields[deIndex - 1], fields[deIndex], fields[deIndex + 1]);

        Date newEventDate;
        try {
            newEventDate = new SimpleDateFormat(datePattern).parse(textContent);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        int year = CalendarUtils.getInstance().getYear();
        newEventDate = DateUtils.setYears(newEventDate, year);
        if (DateUtils.truncatedCompareTo(new Date(), newEventDate, java.util.Calendar.DATE) > 0) {
            newEventDate = DateUtils.addYears(newEventDate, 1);
        }
        if (eventDate == null) {
            eventDate = newEventDate;
        } else if (eventDate.after(newEventDate)) {
            eventDate = DateUtils.addYears(newEventDate, 1);
        }
        return eventDate;
    }
}
