// CsvLoaderService.java
package uk.go.hm.icb.service.lev;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import uk.go.hm.icb.dto.LEVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class LEVDataLoader {

    private static final String CSV_FILE_PATH = "classpath:lev_records.csv";
    @Getter
    private final List<LEVRecord> records = new ArrayList<>();

    private final ResourceLoader resourceLoader;

    @Autowired
    public LEVDataLoader(ResourceLoader resourceLoader) {
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
                    String[] values = line.split(",", -1);
                    records.add(new LEVRecord(
                        values[0],  // firstName
                        values[1],  // middleName
                        values[2],  // lastName
                        LocalDate.parse(values[3], formatter),  // dateOfBirth
                        values[4],  // address
                        values[5],  // birthCertificateNumber
                        values[6],  // flag
                        values[7],  // motherName
                        values[8],  // motherMaidenName
                        values[9],  // motherPlaceOfBirth
                        values[10], // fatherName
                        values[11], // fatherPlaceOfBirth
                        values[12], // registrationDistrict
                        values[13], // subDistrict
                        values[14], // administrativeArea
                        LocalDate.parse(values[15], formatter) // dateOfRegistration
                    ));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error loading LEV records ", e);
        }
    }

}