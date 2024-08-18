package org.doral.sportcalendars.webscraper.runner;

import org.doral.sportcalendars.webscraper.model.calendar.Calendar;
import org.doral.sportcalendars.webscraper.model.readme.ReadmeItem;
import org.doral.sportcalendars.webscraper.runner.exception.WebScraperAppException;
import org.doral.sportcalendars.webscraper.util.calendar.CalendarUtils;
import org.doral.sportcalendars.webscraper.writer.ICal4jWriter;
import org.doral.sportcalendars.webscraper.writer.exception.CalendarWriterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author odoral
 */
public abstract class AbstractBaseScraperCommand implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractBaseScraperCommand.class);

    @CommandLine.Option(names = {"-pd", "--project-directory"}, required = true)
    protected File projectDirectory;

    protected File getOutputFile(String calendarName, String... path) throws WebScraperAppException {
        File sportOutputDirectory = Paths.get(Paths.get(projectDirectory.getAbsolutePath(), "calendars").toString(), path).toFile();
        if (sportOutputDirectory.mkdirs() || sportOutputDirectory.exists()) {
            return new File(sportOutputDirectory,
                    CalendarUtils.getCalendarFileName(calendarName));
        } else {
            throw new WebScraperAppException("Cannot create output folder: " + sportOutputDirectory.getAbsolutePath());
        }
    }

    protected List<ReadmeItem> writeCalendars(String sportType, List<Calendar> calendars) throws WebScraperAppException, IOException, CalendarWriterException {
        List<ReadmeItem> itemsWritten = new ArrayList<>();

        calendars.sort(Comparator.comparing(Calendar::getName));

        // Write full set
        File outputFile = getOutputFile(getCalendarFullSetFileName(sportType), sportType);
        LOGGER.info("Writing full set into {}", outputFile.getAbsoluteFile());
        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            new ICal4jWriter().write(
                    outputStream,
                    "Sport Events App",
                    calendars.stream()
                            .filter(calendar -> Calendar.SortType.BY_TOURNAMENT.equals(calendar.getSortType()))
                            .toArray(Calendar[]::new)
            );
        }
        itemsWritten.add(ReadmeItem.builder()
                .file(outputFile)
                .description(String.join(" ", "All", sportType, "events"))
                .sortType(ReadmeItem.SortType.ALL)
                .build());

        // Write tournament calendars
        itemsWritten.addAll(writeCalendarsByType(sportType, calendars));

        // Write team calendars
        itemsWritten.addAll(writeCalendarsByType(sportType, CalendarUtils.buildCalendarsByTeam(calendars)));

        return itemsWritten;
    }

    protected List<ReadmeItem> writeCalendarsByType(String sportType, List<Calendar> calendars) throws IOException, CalendarWriterException {
        List<ReadmeItem> itemsWritten = new ArrayList<>();

        for (Calendar calendar : calendars) {
            File outputFile = null;
            switch (calendar.getSortType()) {
                case BY_TOURNAMENT -> outputFile = getOutputFile(calendar.getName(), sportType);
                case BY_TEAM -> outputFile = getOutputFile(calendar.getName(), sportType, calendar.getSortType().name().toLowerCase());
            }
            LOGGER.info("Writing calendar: {} into {}", calendar.getName(), outputFile.getAbsoluteFile());
            try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                new ICal4jWriter().write(outputStream, "Sport Events App", calendar);
            }
            itemsWritten.add(ReadmeItem.builder()
                    .file(outputFile)
                    .description(calendar.getName())
                    .sortType(ReadmeItem.SortType.valueOf(calendar.getSortType().name()))
                    .build());
        }

        return itemsWritten;
    }

    protected String getCalendarFullSetFileName(String sportType) {
        return sportType + "_full_set";
    }
}
