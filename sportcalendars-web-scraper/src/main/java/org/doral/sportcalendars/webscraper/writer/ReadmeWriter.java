package org.doral.sportcalendars.webscraper.writer;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.doral.sportcalendars.webscraper.model.readme.ReadmeItem;
import org.doral.sportcalendars.webscraper.runner.exception.WebScraperAppException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author odoral
 */
public class ReadmeWriter implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReadmeWriter.class);
    private static final String GITHUB_RAW_BASE_PATH = "https://raw.githubusercontent.com/odoral/sportscalendars/master/";

    public static final LoadingCache<File, ReadmeWriter> README_WRITER_LOADING_CACHE = CacheBuilder.newBuilder()
            .build(new CacheLoader<>() {
                @Override
                public ReadmeWriter load(File projectBaseDirectory) throws Exception {
                    ReadmeWriter readmeWriter = new ReadmeWriter(projectBaseDirectory);
                    readmeWriter.init();
                    return readmeWriter;
                }
            });

    public static final LoadingCache<File, ReadmeWriter> NO_README_WRITER_LOADING_CACHE = CacheBuilder.newBuilder()
            .build(new CacheLoader<>() {
                @Override
                public ReadmeWriter load(File projectBaseDirectory) throws Exception {
                    return new ReadmeWriter(projectBaseDirectory) {
                        @Override
                        public BufferedWriter init() {
                            // Nothing
                            return null;
                        }

                        @Override
                        public ReadmeWriter writeCalendars(String sport, List<ReadmeItem> calendars) {
                            // Nothing
                            return this;
                        }

                        @Override
                        public void close() throws IOException {
                            // Nothing
                        }
                    };
                }
            });

    private final File projectBaseDirectory;
    private final BufferedWriter writer;

    public ReadmeWriter(File projectBaseDirectory) {
        this.projectBaseDirectory = projectBaseDirectory;
        this.writer = init();
    }

    public BufferedWriter init() {
        try {
            BufferedWriter writer = new BufferedWriter(
                    new FileWriter(
                            new File(projectBaseDirectory, "README.md")));
            IOUtils.copy(new FileInputStream("doc/partial/readme_header.md"), writer, UTF_8);

            return writer;
        } catch (IOException e) {
            throw new WebScraperAppException(e);
        }
    }

    public ReadmeWriter writeCalendars(String sport, List<ReadmeItem> readmeItems) {
        LOGGER.info("Writing readme for {} with {} items.", sport, readmeItems.size());
        try {
            writer.write("## " + StringUtils.capitalize(sport));
            writer.newLine();

            Map<ReadmeItem.SortType, List<ReadmeItem>> itemsSorted = readmeItems.stream().collect(Collectors.groupingBy(ReadmeItem::getSortType));

            // All item
            writeReadmeItemsBullet(itemsSorted.get(ReadmeItem.SortType.ALL));

            // Tournament items
            writer.write("### By Tournament");
            writer.newLine();
            writeReadmeItemsTable(itemsSorted.get(ReadmeItem.SortType.BY_TOURNAMENT));

            // Team items
            writer.write("### By Team");
            writer.newLine();
            writeReadmeItemsTable(itemsSorted.get(ReadmeItem.SortType.BY_TEAM));

        } catch (IOException e) {
            throw new WebScraperAppException(e);
        }

        return this;
    }

    protected void writeReadmeItemsBullet(List<ReadmeItem> readmeItems) throws IOException {
        readmeItems.stream()
                .sorted(Comparator.comparing(ReadmeItem::getDescription))
                .forEach(readmeItem -> {
                    try {
                        writer.write("- ");
                        writer.write(readmeItem.getDescription());
                        writer.write(" [Download ICS]");
                        String githubRawURL = getGithubRawURL(projectBaseDirectory, readmeItem.getFile());
                        writer.write("(" + githubRawURL + ")");
                        writer.write(" ");
                        writer.write(" [Add to Google Calendar]");
                        writer.write("(https://calendar.google.com/calendar/r?cid=" + githubRawURL.replace("https", "webcal") + ")");
                        writer.newLine();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        writer.newLine();
    }

    protected void writeReadmeItemsTable(List<ReadmeItem> readmeItems) throws IOException {
        if (readmeItems != null) {
            writer.write("|Name|Action #1|Action #2|");
            writer.newLine();
            writer.write("|----|---------|---------|");
            writer.newLine();
            readmeItems.stream()
                    .sorted(Comparator.comparing(ReadmeItem::getDescription))
                    .forEach(readmeItem -> {
                        try {
                            writer.write("|");
                            writer.write(readmeItem.getDescription());
                            writer.write("|");
                            writer.write(" [Download ICS]");
                            String githubRawURL = getGithubRawURL(projectBaseDirectory, readmeItem.getFile());
                            writer.write("(" + githubRawURL + ")");
                            writer.write("|");
                            writer.write(" [Add to Google Calendar]");
                            writer.write("(https://calendar.google.com/calendar/r?cid=" + githubRawURL.replace("https", "webcal") + ")");
                            writer.write("|");
                            writer.newLine();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
            writer.newLine();
        }
    }

    protected String getGithubRawURL(File projectBaseDirectory, File calendarFile) {
        String relativePath = projectBaseDirectory.toURI().relativize(calendarFile.toURI()).getPath();
        return GITHUB_RAW_BASE_PATH.concat(relativePath);
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
