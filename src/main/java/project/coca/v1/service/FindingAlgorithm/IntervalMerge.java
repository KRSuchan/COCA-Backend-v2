package project.coca.v1.service.FindingAlgorithm;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class IntervalMerge {
    public static List<Interval> intervalMerge(List<Interval> intervals) {
        if (intervals.size() <= 1)
            return intervals;

        // 날짜와 시간을 기준으로 간격 정렬
        Collections.sort(intervals, Comparator.comparing(Interval::getStart));

        List<Interval> merged = new ArrayList<>();
        LocalDateTime start = intervals.get(0).getStart();
        LocalDateTime end = intervals.get(0).getEnd();

        for (Interval interval : intervals) {
            if (!interval.getStart().isAfter(end)) {
                // 현재 간격의 시작 시간이 이전 간격의 종료 시간 이전이거나 같으면, 간격들이 겹칩니다.
                end = max(end, interval.getEnd());
            } else {
                // 겹치지 않는 경우, 현재까지의 병합된 간격을 추가하고, 새로운 시작과 종료 시간을 설정합니다.
                merged.add(new Interval(start, end));
                start = interval.getStart();
                end = interval.getEnd();
            }
        }

        // 마지막으로 병합된 간격을 추가합니다.
        merged.add(new Interval(start, end));
        return merged;
    }

    private static LocalDateTime max(LocalDateTime a, LocalDateTime b) {
        return a.isAfter(b) ? a : b;
    }
}
