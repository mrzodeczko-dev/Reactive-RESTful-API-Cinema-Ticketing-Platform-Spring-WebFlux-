package com.rzodeczko.application.service.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateTimeGapFinder {

    public static List<Interval> findGaps(List<Interval> existingIntervals, Interval searchInterval) {
        List<Interval> gaps = new ArrayList<>();

        if (existingIntervals.isEmpty()) {
            gaps.add(searchInterval);
            return gaps;
        }

        DateTime searchStart = searchInterval.getStart();
        DateTime searchEnd = searchInterval.getEnd();

        List<Interval> sortedExistingIntervals = existingIntervals.stream()
                .sorted(Comparator.comparing(Interval::getStart))
                .toList();

        if (hasNoOverlap(sortedExistingIntervals, searchStart, searchEnd)) {
            gaps.add(searchInterval);
            return gaps;
        }

        List<Interval> subExistingList = removeNoneOverlappingIntervals(sortedExistingIntervals, searchInterval);
        if (subExistingList.isEmpty()) {
            gaps.add(searchInterval);
            return gaps;
        }

        DateTime subEarliestStart = subExistingList.getFirst().getStart();
        DateTime subLatestStop = subExistingList.getLast().getEnd();


        if (searchStart.isBefore(subEarliestStart)) {
            gaps.add(new Interval(searchStart, subEarliestStart));
        }

        gaps.addAll(getExistingIntervalGaps(subExistingList));

        if (searchEnd.isAfter(subLatestStop)) {
            gaps.add(new Interval(subLatestStop, searchEnd));
        }
        return gaps;
    }

    private static List<Interval> getExistingIntervalGaps(List<Interval> existingList) {
        List<Interval> gaps = new ArrayList<>();
        Interval current = existingList.getFirst();
        for (int i = 1; i < existingList.size(); i++) {
            Interval next = existingList.get(i);
            Interval gap = current.gap(next);
            if (gap != null)
                gaps.add(gap);
            current = next;
        }
        return gaps;
    }

    private static List<Interval> removeNoneOverlappingIntervals(List<Interval> existingIntervals, Interval searchInterval) {
        List<Interval> subExistingList = new ArrayList<>();
        for (Interval interval : existingIntervals) {
            if (interval.overlaps(searchInterval)) {
                subExistingList.add(interval);
            }
        }
        return subExistingList;
    }

    private static boolean hasNoOverlap(List<Interval> existingIntervals, DateTime searchStart, DateTime searchEnd) {
        DateTime earliestStart = existingIntervals.getFirst().getStart();
        DateTime latestStop = existingIntervals.getLast().getEnd();
        return searchEnd.isBefore(earliestStart) || searchStart.isAfter(latestStop);
    }
}
