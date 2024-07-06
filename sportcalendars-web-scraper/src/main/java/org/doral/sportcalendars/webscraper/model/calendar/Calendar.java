package org.doral.sportcalendars.webscraper.model.calendar;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import java.util.List;

/**
 * @author odoral
 */
@Value
@Builder
@ToString
@EqualsAndHashCode
public class Calendar {

    public enum SortType {
        BY_TOURNAMENT,
        BY_TEAM
    }

    String name;
    SortType sortType;
    List<SportEvent> events;

    public static Calendar merge(Calendar calendar1, Calendar calendar2) {
        if (calendar1 != null) {
            if (calendar2 != null) {
                assert calendar1.name.equals(calendar2.name);
                assert calendar1.sortType.equals(calendar2.sortType);
                calendar1.events.addAll(calendar2.events);
            }
            return calendar1;
        } else {
            return calendar2;
        }
    }
}
