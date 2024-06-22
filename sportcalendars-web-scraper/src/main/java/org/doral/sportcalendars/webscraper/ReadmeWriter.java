package org.doral.sportcalendars.webscraper;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.collections4.KeyValue;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.doral.sportcalendars.webscraper.runner.exception.WebScraperAppException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;

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
                        public ReadmeWriter writeCalendars(String sport, List<KeyValue<File, String>> calendars) {
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

    public ReadmeWriter writeCalendars(String sport, List<KeyValue<File, String>> calendars) {
        LOGGER.info("Writing readme for {} with {} calendars.", sport, calendars.size());
        try {
            writer.write("## " + StringUtils.capitalize(sport));
            writer.newLine();

            for (KeyValue<File, String> calendar : calendars) {
                writer.write("- ");
                writer.write(calendar.getValue());
                writer.write(" [ICS]");
                String githubRawURL = getGithubRawURL(projectBaseDirectory, calendar.getKey());
                writer.write("(" + githubRawURL + ")");
                writer.write(" [Google Calendar]");
                writer.write("(https://calendar.google.com/calendar/r?cid=" + githubRawURL + ")");
                writer.newLine();
            }
            writer.newLine();

        } catch (IOException e) {
            throw new WebScraperAppException(e);
        }

        return this;
    }

    private String getGithubRawURL(File projectBaseDirectory, File calendarFile) {
        String relativePath = projectBaseDirectory.toURI().relativize(calendarFile.toURI()).getPath();
        return GITHUB_RAW_BASE_PATH.concat(relativePath);
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
