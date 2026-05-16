package com.rzodeczko.application.service.util;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DateTimeGapFinderTest {

    @Nested
    @DisplayName("findGaps()")
    class FindGapsTests {

        @Test
        @DisplayName("No existing intervals: whole search interval is free")
        void shouldReturnWholeSearchIntervalWhenExistingIntervalsAreEmpty() {
            Interval searchInterval = interval(10, 12);

            assertThat(DateTimeGapFinder.findGaps(List.of(), searchInterval))
                    .containsExactly(searchInterval);
        }

        @Test
        @DisplayName("No overlap outside schedule bounds: whole search interval is free")
        void shouldReturnWholeSearchIntervalWhenSearchIntervalHasNoOverlapOutsideBounds() {
            Interval searchInterval = interval(6, 8);
            List<Interval> existingIntervals = List.of(interval(10, 12), interval(14, 16));

            assertThat(DateTimeGapFinder.findGaps(existingIntervals, searchInterval))
                    .containsExactly(searchInterval);
        }

        @Test
        @DisplayName("No overlap inside schedule bounds: whole search interval is free")
        void shouldReturnWholeSearchIntervalWhenSearchIntervalFallsBetweenExistingIntervals() {
            Interval searchInterval = interval(12, 14);
            List<Interval> existingIntervals = List.of(interval(10, 11), interval(15, 16));

            assertThat(DateTimeGapFinder.findGaps(existingIntervals, searchInterval))
                    .containsExactly(searchInterval);
        }

        @Test
        @DisplayName("Existing intervals inside search: leading, middle and trailing gaps returned")
        void shouldReturnLeadingMiddleAndTrailingGaps() {
            Interval searchInterval = interval(9, 18);
            List<Interval> existingIntervals = List.of(interval(10, 12), interval(14, 16));

            assertThat(DateTimeGapFinder.findGaps(existingIntervals, searchInterval))
                    .containsExactly(
                            interval(9, 10),
                            interval(12, 14),
                            interval(16, 18)
                    );
        }

        @Test
        @DisplayName("Adjacent existing intervals: no artificial gap")
        void shouldNotReturnGapBetweenAdjacentIntervals() {
            Interval searchInterval = interval(10, 14);
            List<Interval> existingIntervals = List.of(interval(10, 12), interval(12, 14));

            assertThat(DateTimeGapFinder.findGaps(existingIntervals, searchInterval)).isEmpty();
        }

        @Test
        @DisplayName("Unsorted existing intervals: intervals sorted before gap calculation")
        void shouldHandleUnsortedExistingIntervals() {
            Interval searchInterval = interval(9, 18);
            List<Interval> existingIntervals = List.of(interval(14, 16), interval(10, 12));

            assertThat(DateTimeGapFinder.findGaps(existingIntervals, searchInterval))
                    .containsExactly(
                            interval(9, 10),
                            interval(12, 14),
                            interval(16, 18)
                    );
        }
    }

    private Interval interval(int startHour, int endHour) {
        return new Interval(dateTime(startHour), dateTime(endHour));
    }

    private DateTime dateTime(int hour) {
        return new DateTime(2026, 5, 16, hour, 0);
    }
}
