package com.example.icb.loader;

import org.springframework.stereotype.Component;
import org.springframework.cache.annotation.Cacheable;
import java.util.List;

@Component
public abstract class BaseDataLoader<T> {
    @Cacheable("records")
    public List<T> getRecords() {
        return loadRecords();
    }

    protected abstract List<T> loadRecords();

    protected void validateRecord(String[] values, int expectedLength) {
        if (values.length != expectedLength) {
            throw new IllegalArgumentException("Invalid record format");
        }
    }
}
