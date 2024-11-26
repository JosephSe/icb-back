package com.example.icb.util;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CollectionUtils {
    public static <T> List<T> findMatchingRecords(List<T> records, Predicate<T> criteria) {
        return records.parallelStream()
            .filter(criteria)
            .collect(Collectors.toList());
    }
}
