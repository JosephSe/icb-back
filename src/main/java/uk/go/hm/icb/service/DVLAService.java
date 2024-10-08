package uk.go.hm.icb.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import uk.go.hm.icb.dto.DrivingLicenceRecord;

@Service
public class DVLAService {
    
    private static final String CSV_FILE_PATH = "classpath:driving_licence_records.csv";
    private List<DrivingLicenceRecord> records;

    private final ResourceLoader resourceLoader;

    @Autowired
    public DVLAService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void init() {
        loadRecords();
    }

    private void loadRecords() {
        records = new ArrayList<>();
        try {
            Resource resource = resourceLoader.getResource(CSV_FILE_PATH);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
                String line;
                br.readLine(); // Skip header
                while ((line = br.readLine()) != null) {
                    String[] values = line.split(",");
                    records.add(new DrivingLicenceRecord(values[0], values[1], values[2]));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error loading driving licence records", e);
        }
    }

    public List<DrivingLicenceRecord> searchByLastName(String lastName) {
        return records.stream()
                .filter(record -> record.getLastName().equalsIgnoreCase(lastName))
                .collect(Collectors.toList());
    }

}
