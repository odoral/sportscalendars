package org.doral.sportcalendars.webscraper.writer;

import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Uid;
import org.doral.sportcalendars.webscraper.model.calendar.SportEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * @author odoral
 */
class ICal4jWriterTest {

    private final ICal4jWriter writer = new ICal4jWriter();

    @Test
    void uidIsSameWhenOnlyHourChangesOnSameUtcDay() {
        VEvent morning = writer.toVEvent(eventAt("2026-11-23T09:00:00Z"));
        VEvent evening = writer.toVEvent(eventAt("2026-11-23T21:00:00Z"));

        assertEquals(uid(morning), uid(evening));
    }

    @Test
    void uidChangesWhenDayChanges() {
        VEvent firstDay = writer.toVEvent(eventAt("2026-11-23T09:00:00Z"));
        VEvent nextDay = writer.toVEvent(eventAt("2026-11-24T09:00:00Z"));

        assertNotEquals(uid(firstDay), uid(nextDay));
    }

    @Test
    void uidStillNormalizesNameAndTeams() {
        Instant start = Instant.parse("2026-11-23T09:00:00Z");
        VEvent untrimmed = writer.toVEvent(event("  Partido  ", start, " Equipo A ", null));
        VEvent normalized = writer.toVEvent(event("Partido", start, "Equipo A", ""));

        assertEquals(uid(untrimmed), uid(normalized));
    }

    private static SportEvent eventAt(String instant) {
        return event("Partido", Instant.parse(instant), "Equipo A", "Equipo B");
    }

    private static SportEvent event(String name, Instant start, String teamA, String teamB) {
        return SportEvent.builder()
                .name(name)
                .startTimestamp(Date.from(start))
                .teamA(teamA)
                .teamB(teamB)
                .channels(Collections.emptyList())
                .build();
    }

    private static String uid(VEvent vEvent) {
        return ((Uid) vEvent.getProperty("UID")).getValue();
    }
}
