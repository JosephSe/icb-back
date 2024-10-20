package uk.go.hm.icb.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import uk.go.hm.icb.dto.DrivingLicenceRecord;
import uk.go.hm.icb.dto.ICBMatch;
import uk.go.hm.icb.dto.ICBMultiMatch;
import uk.go.hm.icb.dto.ICBRequest;
import uk.go.hm.icb.dto.ICBResponse;
import uk.go.hm.icb.dto.SearchIDType;
import uk.go.hm.icb.dto.SearchIdentifiers;

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
                    records.add(new DrivingLicenceRecord(values[0], values[1], values[2], values[3], values[4], values[5]));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error loading driving licence records", e);
        }
    }

    /**
     * filter on DVLA records first on DL Number, then on first, last and middle (if it exists) names
     * filtering on other fields should also be added if they are in the input request
     * */
    public ICBResponse search(ICBRequest request) {
        ICBResponse.ICBResponseBuilder responseBuilder = ICBResponse.builder();
        Optional<SearchIdentifiers> searchDLIdentifiers = Optional.ofNullable(request.getSearchIDTypes()).orElse(List.of())
                .stream().filter(t -> SearchIDType.DRIVER_LICENSE == t.getSearchIDType()).findFirst();
        List<DrivingLicenceRecord> list = records.stream().filter(rec -> searchDLIdentifiers.map(si -> si.getValue().equalsIgnoreCase(rec.getDrivingLicenseNumber())).orElse(true)
                )
                .filter(rec -> Optional.ofNullable(request.getSearchBioDetails().getFirstName()).map(f -> f.equalsIgnoreCase(rec.getFirstName())).orElse(false))
                .filter(rec -> Optional.ofNullable(request.getSearchBioDetails().getLastName()).map(f -> f.equalsIgnoreCase(rec.getLastName())).orElse(false))
                .filter(rec -> Optional.ofNullable(request.getSearchBioDetails().getMiddleName()).map(f -> f.equalsIgnoreCase(rec.getMiddleName())).orElse(true))
                .toList();
        if (list.size() == 1) {
            ICBMatch.ICBMatchBuilder matchBuilder = ICBMatch.builder().firstNameMatched("YES").lastNameMatched("YES");
            DrivingLicenceRecord record = list.get(0);
            Optional.ofNullable(record.getDrivingLicenseNumber()).map(dl->dl.equalsIgnoreCase(searchDLIdentifiers.map(SearchIdentifiers::getValue).orElse(""))).map(b-> b ? "YES" : "NO").ifPresent(matchBuilder::drivingLicenseNumberMatched);
            Optional.ofNullable(record.getMiddleName()).map(mn->mn.equalsIgnoreCase(request.getSearchBioDetails().getMiddleName())).map(b-> b ? "YES" : "NO").ifPresent(matchBuilder::middleNameMatched);
            responseBuilder.match(matchBuilder.build()).verifications(List.of("Name Match - 100%", "DL Match - 100%"));
        } else if (list.size() > 1) {
            responseBuilder.multiMatches(
            list.stream().map(rec -> ICBMultiMatch.builder().firstName(rec.getFirstName())
                    .lastName(rec.getLastName()).middleName(rec.getMiddleName())
                    .dateOfBirth(rec.getDateOfBirth()).address(rec.getAddress())
                    .build()).toList());
        }
        return responseBuilder.searchComplete(true).build();
    }

    public List<DrivingLicenceRecord> searchByLastName(String lastName) {
        return records.stream()
                .filter(record -> record.getLastName().equalsIgnoreCase(lastName))
                .collect(Collectors.toList());
    }

}
