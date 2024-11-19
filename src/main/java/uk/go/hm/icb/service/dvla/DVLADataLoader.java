package uk.go.hm.icb.service.dvla;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import uk.go.hm.icb.dto.DrivingLicenceRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class DVLADataLoader {

    private static final String CSV_FILE_PATH = "classpath:driving_licence_records.csv";
    private final ResourceLoader resourceLoader;

    @Getter
    private final List<DrivingLicenceRecord> records = new ArrayList<>();

    public DVLADataLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public List<DrivingLicenceRecord> loadRecords() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        try {
            Resource resource = resourceLoader.getResource(CSV_FILE_PATH);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
                String line;
                br.readLine(); // Skip header
                while ((line = br.readLine()) != null) {
                    String[] values = line.split(",");
                    records.add(new DrivingLicenceRecord(values[0], values[1], values[2], LocalDate.parse(values[3], formatter), values[4], values[5] ,values[6]));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error loading driving licence records", e);
        }
        return records;
    }
}