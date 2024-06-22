package org.doral.sportcalendars.webscraper.runner;

import org.apache.commons.collections4.KeyValue;
import org.doral.sportcalendars.webscraper.ReadmeWriter;
import org.doral.sportcalendars.webscraper.model.Calendar;
import org.doral.sportcalendars.webscraper.runner.exception.WebScraperAppException;
import org.doral.sportcalendars.webscraper.site.BaloncestoHoyWebScraper;
import org.doral.sportcalendars.webscraper.site.FutbolHoyWebScraper;
import org.doral.sportcalendars.webscraper.site.exception.SiteWebScraperException;
import org.doral.sportcalendars.webscraper.writer.exception.CalendarWriterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author odoral
 */
@CommandLine.Command
public class WebScraperApp extends AbstractBaseScraperCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebScraperApp.class);

    public static final String FOOTBALL = "football";
    public static final String BASKET = "basket";

    @CommandLine.Option(names = {"-gr", "--generate-readme"}, required = false)
    protected boolean generateReadme;

    public static void main(String[] args) throws SiteWebScraperException, IOException, CalendarWriterException {
        new CommandLine(new WebScraperApp()).execute(args);
    }

    @CommandLine.Command(name = FOOTBALL)
    public List<KeyValue<File, String>> parseFootball() {
        LOGGER.info("Running command football");
        try {
            List<Calendar> calendars = new FutbolHoyWebScraper().parseCalendars();
            return writeCalendars(FOOTBALL, calendars);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @CommandLine.Command(name = BASKET)
    public List<KeyValue<File, String>> parseBasket() {
        LOGGER.info("Running command basket");
        try {
            List<Calendar> calendars = new BaloncestoHoyWebScraper().parseCalendars();
            return writeCalendars(BASKET, calendars);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        try (ReadmeWriter readmeWriter = (generateReadme ? ReadmeWriter.README_WRITER_LOADING_CACHE : ReadmeWriter.NO_README_WRITER_LOADING_CACHE)
                .getUnchecked(projectDirectory)
                .writeCalendars(FOOTBALL, parseFootball())
                .writeCalendars(BASKET, parseBasket())) {
            LOGGER.info("Readme completed.");
        } catch (IOException e) {
            throw new WebScraperAppException(e);
        }
    }

}
