package uk.go.hm.icb.service.dvla;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.go.hm.icb.dto.DrivingLicenceRecord;
import uk.go.hm.icb.dto.ICBMatch;
import uk.go.hm.icb.dto.ICBMultiMatch;
import uk.go.hm.icb.dto.ICBRequest;
import uk.go.hm.icb.dto.ICBResponse;
import uk.go.hm.icb.dto.SearchBioDetails;
import uk.go.hm.icb.dto.SearchIDType;
import uk.go.hm.icb.dto.SearchIdentifiers;
import uk.go.hm.icb.dto.SearchSource;
import uk.go.hm.icb.service.SearchStrategy;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DVLAService implements SearchStrategy {
    
    private static final String CSV_FILE_PATH = "classpath:driving_licence_records.csv";

    private final DVLADataLoader recordLoader;

    private final long delay;

    @Autowired
    public DVLAService(DVLADataLoader recordLoader, @Value("${app.dvla.delay}") long delay) {
        this.recordLoader = recordLoader;
        this.delay = delay;
    }

    /**
     * filter on DVLA records first on DL Number, then on first, last and middle (if it exists) names
     * filtering on other fields should also be added if they are in the input request
     * */
    @Override
    public ICBResponse search(ICBRequest request) {
        ICBResponse.ICBResponseBuilder responseBuilder = ICBResponse.builder().searchSource(SearchSource.DVLA);
        Optional<SearchIdentifiers> searchDLIdentifiers = Optional.ofNullable(request.getSearchIDTypes()).orElse(List.of())
                .stream().filter(t -> SearchIDType.DRIVER_LICENSE == t.getIdType()).filter(t -> StringUtils.hasText(t.getIdValue()))
                .findFirst();
        List<DrivingLicenceRecord> dvlaRecords = recordLoader.getRecords();
        List<DrivingLicenceRecord> list = List.of();
        if (searchDLIdentifiers.isPresent()) {
            list = dvlaRecords.stream()
                    .filter(rec -> searchDLIdentifiers
                            .map(si -> si.getIdValue().equalsIgnoreCase(rec.getDrivingLicenseNumber()))
                            .orElse(false))
                    .toList();
        }
        if (list.isEmpty()){
            list = dvlaRecords.stream()
                    .filter(rec -> Optional.ofNullable(request.getSearchBioDetails().getLastName())
                            .map(f -> f.equalsIgnoreCase(rec.getLastName())).orElse(false))
                    .toList();
        }

        if (list.isEmpty()) {
            responseBuilder.matchStatus("No match found");
        } else if (list.size() == 1) {
            ICBMatch.ICBMatchBuilder matchBuilder = ICBMatch.builder();
            DrivingLicenceRecord record = list.get(0);

            String dvlaMatched = searchDLIdentifiers.map(SearchIdentifiers::getIdValue)
                    .map(dl -> dl.equalsIgnoreCase(record.getDrivingLicenseNumber()))
                    .map(b -> b ? "YES" : "NO").orElse("-");
            String firstNameMatched = Optional.ofNullable(request.getSearchBioDetails()).map(SearchBioDetails::getFirstName)
                    .filter(StringUtils::hasText)
                    .map(mn -> mn.equalsIgnoreCase(record.getFirstName()))
                    .map(b -> b ? "YES" : "NO").orElse("-");
            String lastNameMatched = Optional.ofNullable(request.getSearchBioDetails()).map(SearchBioDetails::getLastName)
                    .filter(StringUtils::hasText)
                    .map(mn -> mn.equalsIgnoreCase(record.getLastName()))
                    .map(b -> b ? "YES" : "NO").orElse("-");
            String middleNameMatched = Optional.ofNullable(request.getSearchBioDetails()).map(SearchBioDetails::getMiddleName)
                    .filter(StringUtils::hasText)
                    .map(mn -> mn.equalsIgnoreCase(record.getMiddleName()))
                    .map(b -> b ? "YES" : "NO").orElse("-");
            String dobMatched = Optional.ofNullable(record.getDateOfBirth())
                    .map(mn -> mn.isEqual(Optional.ofNullable(request.getSearchBioDetails()).map(SearchBioDetails::getDateOfBirth).orElse(LocalDate.now())))
                    .map(b -> b ? "YES" : "NO")
                    .orElse("-");

            matchBuilder.matches(firstNameMatched, lastNameMatched, middleNameMatched, dobMatched, "-", "-", dvlaMatched);
            responseBuilder.matchStatus("One match found").match(matchBuilder.build());
        } else {
            responseBuilder.matchStatus("Multiple matches found")
                    .multiMatches(
                            list.stream().map(rec -> ICBMultiMatch.builder()
                                    .firstName(rec.getFirstName())
                                    .lastName(rec.getLastName())
                                    .middleName(rec.getMiddleName())
                                    .dateOfBirth(rec.getDateOfBirth())
                                    .address(rec.getAddress())
                                    .drivingLicenseNumber(rec.getDrivingLicenseNumber())
                                    .build()).toList());
        }
        return responseBuilder.searchComplete(true).build();
    }

    @Override
    public long getDelay() {
        return delay;
    }

    public List<DrivingLicenceRecord> searchByLastName(String lastName) {
        return recordLoader.getRecords().stream()
                .filter(record -> record.getLastName().equalsIgnoreCase(lastName))
                .collect(Collectors.toList());
    }

}
