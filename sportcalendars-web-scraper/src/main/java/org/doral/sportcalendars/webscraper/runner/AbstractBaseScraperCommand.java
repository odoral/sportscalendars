package org.doral.sportcalendars.webscraper.runner;

import org.apache.commons.collections4.KeyValue;
import org.apache.commons.collections4.keyvalue.DefaultKeyValue;
import org.doral.sportcalendars.webscraper.model.Calendar;
import org.doral.sportcalendars.webscraper.runner.exception.WebScraperAppException;
import org.doral.sportcalendars.webscraper.writer.ICal4jWriter;
import org.doral.sportcalendars.webscraper.writer.exception.CalendarWriterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * @author odoral
 */
public abstract class AbstractBaseScraperCommand implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractBaseScraperCommand.class);

    @CommandLine.Option(names = {"-pd", "--project-directory"}, required = true)
    protected File projectDirectory;

    protected File getOutputFile(String sport, String calendarName) throws WebScraperAppException {
        File sportOutputDirectory = new File(new File(projectDirectory, "calendars"), sport);
        if (sportOutputDirectory.mkdirs() || sportOutputDirectory.exists()) {
            return new File(sportOutputDirectory,
                    getCalendarFileName(calendarName));
        } else {
            throw new WebScraperAppException("Cannot create output folder: " + sportOutputDirectory.getAbsolutePath());
        }
    }

    protected String getCalendarFileName(String calendarName) {
        return Normalizer.normalize(
                        calendarName.toLowerCase(Locale.ROOT).replaceAll("\\s+", "_"),
                        Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "") + ".ics";
    }

    protected List<KeyValue<File, String>> writeCalendars(String sportType, List<Calendar> calendars) throws WebScraperAppException, IOException, CalendarWriterException {
        List<KeyValue<File, String>> fileCalendars = new ArrayList<>();

        calendars.sort(Comparator.comparing(Calendar::getName));

        // Write full set
        File outputFile = getOutputFile(sportType, getCalendarFullSetFileName(sportType));
        LOGGER.info("Writing full set into {}", outputFile.getAbsoluteFile());
        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            new ICal4jWriter().write(outputStream, "Sport Events App", calendars.toArray(Calendar[]::new));
        }
        fileCalendars.add(new DefaultKeyValue<>(outputFile, String.join(" ", "All", sportType, "events")));


        // Iterate over calendars
        for (Calendar calendar : calendars) {
            outputFile = getOutputFile(sportType, calendar.getName());
            LOGGER.info("Writing calendar: {} into {}", calendar.getName(), outputFile.getAbsoluteFile());
            try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                new ICal4jWriter().write(outputStream, "Sport Events App", calendar);
            }
            fileCalendars.add(new DefaultKeyValue<>(outputFile, calendar.getName()));
        }

        return fileCalendars;
    }

    protected String getCalendarFullSetFileName(String sportType) {
        return sportType + "_full_set";
    }
}
