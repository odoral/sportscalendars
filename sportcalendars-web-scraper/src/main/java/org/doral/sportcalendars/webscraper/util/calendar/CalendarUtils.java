package org.doral.sportcalendars.webscraper.util.calendar;

import org.apache.commons.collections4.keyvalue.AbstractKeyValue;
import org.apache.commons.collections4.keyvalue.DefaultKeyValue;
import org.doral.sportcalendars.webscraper.model.calendar.Calendar;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author odoral
 */
public class CalendarUtils {

    private CalendarUtils() {
    }

    public static List<Calendar> buildCalendarsByTeam(List<Calendar> calendars) {
        return Optional.ofNullable(calendars)
                .orElse(new ArrayList<>())
                .stream()
                .map(Calendar::getEvents)
                .flatMap(List::stream)
                .flatMap(sportEvent -> Stream.of(
                        new DefaultKeyValue<>(sportEvent.getTeamA(), sportEvent),
                        new DefaultKeyValue<>(sportEvent.getTeamB(), sportEvent)
                ))
                .collect(Collectors.groupingBy(
                        AbstractKeyValue::getKey,
                        Collectors.mapping(AbstractKeyValue::getValue, Collectors.toList())))
                .entrySet()
                .stream()
                .map(entry -> Calendar.builder()
                        .name(entry.getKey())
                        .sortType(Calendar.SortType.BY_TEAM)
                        .events(entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }
}
