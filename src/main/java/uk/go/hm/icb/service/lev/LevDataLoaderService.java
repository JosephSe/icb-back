// CsvLoaderService.java
package uk.go.hm.icb.service.lev;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.go.hm.icb.dto.LEVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Component
public class LevDataLoaderService {

    private static final String CSV_FILE_PATH = "classpath:lev_records.csv";

    @Getter
    private final List<LEVRecord> records = new ArrayList<>();

    private final ResourceLoader resourceLoader;

    @Autowired
    public LevDataLoaderService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void init() {
        loadRecords();
    }

    private void loadRecords() {
        try {
            Resource resource = resourceLoader.getResource(CSV_FILE_PATH);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
                String line;
                br.readLine(); // Skip header
                while ((line = br.readLine()) != null) {
                    String[] values = line.split(",");
                    records.add(new LEVRecord(values[0], values[1], values[2], values[3], values[4], values[5]));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error loading driving licence records", e);
        }
    }

}