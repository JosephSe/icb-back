package uk.go.hm.icb.service.dvla;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import uk.go.hm.icb.dto.DrivingLicenceRecord;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TestDVLADataLoader {
    private final List<DrivingLicenceRecord> records;

    public TestDVLADataLoader() {
        this.records = loadTestData();
    }

    public List<DrivingLicenceRecord> getRecords() {
        return records;
    }

    private List<DrivingLicenceRecord> loadTestData() {
        List<DrivingLicenceRecord> testRecords = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new InputStreamReader(
                Objects.requireNonNull(getClass().getResourceAsStream("/dvla_test_data.csv"))))) {
            
            // Skip header
            reader.skip(1);
            
            List<String[]> rows = reader.readAll();
            for (String[] row : rows) {
                testRecords.add(new DrivingLicenceRecord(row[0].trim(), row[1].trim(), row[2].trim(), row[3].trim(),row[4].trim(), row[5].trim()));
            }
        } catch (IOException | CsvException e) {
            throw new RuntimeException("Failed to load test data", e);
        }
        return testRecords;
    }
}