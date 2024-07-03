package org.doral.sportcalendars.webscraper.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import java.util.Date;
import java.util.List;

/**
 * @author odoral
 */
@Value
@Builder
@ToString
@EqualsAndHashCode
public class SportEvent {
    Date startTimestamp;
    String name;
    String teamA;
    String teamB;
    List<Channel> channels;
    String disclaimer;
}
