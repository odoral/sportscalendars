package org.doral.sportcalendars.webscraper.model.readme;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import java.io.File;

/**
 * @author odoral
 */
@Value
@Builder
@ToString
@EqualsAndHashCode
public class ReadmeItem {

    public enum SortType {
        ALL,
        BY_TOURNAMENT,
        BY_TEAM
    }

    File file;
    String description;
    SortType sortType;

}
