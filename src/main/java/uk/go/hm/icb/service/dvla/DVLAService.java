package uk.go.hm.icb.service.dvla;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.go.hm.icb.dto.DrivingLicenceRecord;
import uk.go.hm.icb.dto.ICBMatch;
import uk.go.hm.icb.dto.ICBMultiMatch;
import uk.go.hm.icb.dto.ICBRequest;
import uk.go.hm.icb.dto.ICBResponse;
import uk.go.hm.icb.dto.SearchIDType;
import uk.go.hm.icb.dto.SearchIdentifiers;
import uk.go.hm.icb.dto.SearchSource;

@Service
public class DVLAService {
    
    private static final String CSV_FILE_PATH = "classpath:driving_licence_records.csv";

    private final DrivingLicenceDataLoader recordLoader;

    @Autowired
    public DVLAService(DrivingLicenceDataLoader recordLoader) {
        this.recordLoader = recordLoader;
    }

    /**
     * filter on DVLA records first on DL Number, then on first, last and middle (if it exists) names
     * filtering on other fields should also be added if they are in the input request
     * */
    public ICBResponse search(ICBRequest request) {
        ICBMatch.ICBMatchBuilder matchBuilder = ICBMatch.builder();
        ICBResponse.ICBResponseBuilder responseBuilder = ICBResponse.builder().searchSource(SearchSource.DVLA);
        Optional<SearchIdentifiers> searchDLIdentifiers = Optional.ofNullable(request.getSearchIDTypes()).orElse(List.of())
                .stream().filter(t -> SearchIDType.DRIVER_LICENSE == t.getIdType()).findFirst();
        List<DrivingLicenceRecord> list = recordLoader.getRecords().stream().filter(rec -> searchDLIdentifiers.map(si -> si.getIdValue().equalsIgnoreCase(rec.getDrivingLicenseNumber())).orElse(true)
                )
                .filter(rec -> Optional.ofNullable(request.getSearchBioDetails().getFirstName()).map(f -> f.equalsIgnoreCase(rec.getFirstName())).orElse(false))
                .filter(rec -> Optional.ofNullable(request.getSearchBioDetails().getLastName()).map(f -> f.equalsIgnoreCase(rec.getLastName())).orElse(false))
                .filter(rec -> Optional.ofNullable(request.getSearchBioDetails().getMiddleName()).map(f -> f.equalsIgnoreCase(rec.getMiddleName())).orElse(true))
                .toList();
        if (list.isEmpty()) {
            responseBuilder.matchStatus("No match found");
        } else if (list.size() == 1) {
            DrivingLicenceRecord record = list.get(0);
            String dlMatched = Optional.ofNullable(record.getDrivingLicenseNumber()).map(dl -> dl.equalsIgnoreCase(searchDLIdentifiers.map(SearchIdentifiers::getIdValue).orElse(""))).map(b -> b ? "YES" : "NO").orElse("-");
            String middleNameMatched = Optional.ofNullable(record.getMiddleName()).map(mn -> mn.equalsIgnoreCase(request.getSearchBioDetails().getMiddleName())).map(b -> b ? "YES" : "NO").orElse("-");
            matchBuilder.matches("YES", "YES", middleNameMatched, "YES", "YES", "-", dlMatched);
            responseBuilder.matchStatus("One match found").match(matchBuilder.build());
        } else {
            responseBuilder.matchStatus("Multiple matches found").multiMatches(
            list.stream().map(rec -> ICBMultiMatch.builder().firstName(rec.getFirstName())
                    .lastName(rec.getLastName()).middleName(rec.getMiddleName())
                    .dateOfBirth(rec.getDateOfBirth()).address(rec.getAddress())
                    .build()).toList());
            responseBuilder.match(matchBuilder.build());
        }
        return responseBuilder.searchComplete(true).build();
    }

    public List<DrivingLicenceRecord> searchByLastName(String lastName) {
        return recordLoader.getRecords().stream()
                .filter(record -> record.getLastName().equalsIgnoreCase(lastName))
                .collect(Collectors.toList());
    }

}
