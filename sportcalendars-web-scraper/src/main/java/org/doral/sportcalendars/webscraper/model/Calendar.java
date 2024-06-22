package org.doral.sportcalendars.webscraper.model;

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

    String name;
    List<SportEvent> events;

    public static Calendar merge(Calendar calendar1, Calendar calendar2) {
        if (calendar1 != null) {
            if (calendar2 != null) {
                calendar1.events.addAll(calendar2.events);
            }
            return calendar1;
        } else {
            return calendar2;
        }
    }
}
