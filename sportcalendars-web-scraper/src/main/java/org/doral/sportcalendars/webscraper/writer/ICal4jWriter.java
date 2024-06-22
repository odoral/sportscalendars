package org.doral.sportcalendars.webscraper.writer;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.XParameter;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.XProperty;
import net.fortuna.ical4j.util.RandomUidGenerator;
import org.apache.commons.lang3.time.DateUtils;
import org.doral.sportcalendars.webscraper.model.Channel;
import org.doral.sportcalendars.webscraper.model.SportEvent;
import org.doral.sportcalendars.webscraper.writer.exception.CalendarWriterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author odoral
 */
public class ICal4jWriter implements ICalendarWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ICal4jWriter.class);
    public static final String TIME_ZONE = "Europe/Madrid";

    @Override
    public void write(OutputStream outputStream, String prodId, org.doral.sportcalendars.webscraper.model.Calendar... calendar) throws CalendarWriterException {
        LOGGER.info("Writing {} calendars for {}", calendar.length, prodId);
        FluentCalendar outputCalendar = new Calendar()
                .withProdId("-//" + prodId + "//iCal4j 1.0//ES")
                .withDefaults();

        Stream.of(calendar)
                .flatMap(c -> c.getEvents().stream())
                .sorted(Comparator.comparing(SportEvent::getStartTimestamp))
                .map(this::toVEvent)
                .forEach(outputCalendar::withComponent);

        try {
            new CalendarOutputter().output(outputCalendar.getFluentTarget(), outputStream);
        } catch (IOException e) {
            throw new CalendarWriterException(e.getMessage(), e);
        }
    }

    protected VEvent toVEvent(SportEvent sportEvent) {
        ParameterList htmlParameters = new ParameterList();
        XParameter fmtTypeParameter = new XParameter("FMTTYPE", "text/html");
        htmlParameters.add(fmtTypeParameter);
        String html = toHTML(sportEvent, sportEvent.getChannels());
        XProperty htmlProp = new XProperty("X-ALT-DESC", htmlParameters, html);

        TimeZone timeZone = TimeZoneRegistryFactory.getInstance()
                .createRegistry()
                .getTimeZone(TIME_ZONE);

        return new VEvent(
                new DateTime(sportEvent.getStartTimestamp(), timeZone),
                new DateTime(DateUtils.addHours(sportEvent.getStartTimestamp(), 2), timeZone),
                sportEvent.getName())
                .withProperty(timeZone
                        .getVTimeZone()
                        .getTimeZoneId())
                .withProperty(new RandomUidGenerator().generateUid())
                .withProperty(htmlProp)
                .withProperty(new Description(toPlain(sportEvent, sportEvent.getChannels())))
                .getFluentTarget();
    }

    protected String toHTML(SportEvent sportEvent, List<Channel> channels) {
        return "<html>" +
                "<body>" +
                toHTMLDisclaimer(sportEvent) +
                "<ul>" +
                channels.stream()
                        .sorted(Comparator.comparing(Channel::getType).thenComparing(Channel::getName))
                        .map(channel -> "<li>" + String.join(": ", channel.getType().getDescription(), channel.getName()) + "</li>")
                        .collect(Collectors.joining("\n")) +
                "</ul>" +
                "</body>" +
                "</html>";
    }

    private static String toHTMLDisclaimer(SportEvent sportEvent) {
        return sportEvent.getDisclaimer() != null ? "<b>Atención: </b>" + sportEvent.getDisclaimer() + "\n" : "";
    }

    protected String toPlain(SportEvent sportEvent, List<Channel> channels) {
        return toPlainDisclaimer(sportEvent) +
                channels.stream()
                        .sorted(Comparator.comparing(Channel::getType).thenComparing(Channel::getName))
                        .map(channel -> "- " + String.join(": ", channel.getType().getDescription(), channel.getName()))
                        .collect(Collectors.joining("\n"));
    }

    protected String toPlainDisclaimer(SportEvent sportEvent) {
        return sportEvent.getDisclaimer() != null ? "Atención: " + sportEvent.getDisclaimer() + "\n" : "";
    }
}
