package org.doral.sportcalendars.webscraper.site;

import org.doral.sportcalendars.webscraper.model.Calendar;
import org.doral.sportcalendars.webscraper.site.exception.SiteWebScraperException;

import java.util.List;

/**
 * @author odoral
 */
public interface ISiteWebScraper {
    List<Calendar> parseCalendars() throws SiteWebScraperException;
}
