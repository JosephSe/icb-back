// CsvLoaderService.java
package uk.go.hm.icb.service.ipcs;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import uk.go.hm.icb.dto.IPCSRecord;
import uk.go.hm.icb.dto.LEVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class IPCSDataLoader {

    private static final String CSV_FILE_PATH = "classpath:ipcs_records.csv";

    @Getter
    private final List<IPCSRecord> records = new ArrayList<>();

    private final ResourceLoader resourceLoader;

    @Autowired
    public IPCSDataLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void init() {
        loadRecords();
    }

    private void loadRecords() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        try {
            Resource resource = resourceLoader.getResource(CSV_FILE_PATH);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
                String line;
                br.readLine(); // Skip header
                while ((line = br.readLine()) != null) {
                    String[] values = line.split(",");
                    records.add(new IPCSRecord(values[0], values[1], values[2]));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error loading IPCS records", e);
        }
    }

}