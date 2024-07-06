package org.doral.sportcalendars.webscraper.model.calendar;

import lombok.*;

/**
 * @author odoral
 */
@Value
@Builder
@ToString
@EqualsAndHashCode
public class Channel {

    @Getter
    public enum Type {
        OPEN("Open"),
        PAY_PER_VIEW("PPV");

        private final String description;

        Type(String description) {
            this.description = description;
        }
    }

    Type type;
    String name;
}
