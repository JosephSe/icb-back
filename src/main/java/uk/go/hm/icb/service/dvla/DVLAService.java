package uk.go.hm.icb.service.dvla;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.util.StringUtils;
import uk.go.hm.icb.dto.DrivingLicenceRecord;
import uk.go.hm.icb.dto.ICBMatch;
import uk.go.hm.icb.dto.ICBMultiMatch;
import uk.go.hm.icb.dto.ICBRequest;
import uk.go.hm.icb.dto.ICBResponse;
import uk.go.hm.icb.dto.LEVRecord;
import uk.go.hm.icb.dto.SearchBioDetails;
import uk.go.hm.icb.dto.SearchIDType;
import uk.go.hm.icb.dto.SearchIdentifiers;
import uk.go.hm.icb.dto.SearchSource;

@Service
public class DVLAService {

    private final DVLADataLoader recordLoader;

    @Autowired
    public DVLAService(DVLADataLoader recordLoader) {
        this.recordLoader = recordLoader;
    }

    /**
     * filter on DVLA records first on DL Number, then on first, last and middle (if it exists) names
     * filtering on other fields should also be added if they are in the input request
     * */
    public ICBResponse search(ICBRequest request) {
        ICBResponse.ICBResponseBuilder responseBuilder = ICBResponse.builder().searchSource(SearchSource.DVLA);
        Optional<SearchIdentifiers> searchDLIdentifiers = Optional.ofNullable(request.getSearchIDTypes()).orElse(List.of())
                .stream().filter(t -> SearchIDType.DRIVER_LICENSE == t.getIdType()).filter(t-> StringUtils.hasText(t.getIdValue()))
                .findFirst();
        List<DrivingLicenceRecord> dvlaRecords = recordLoader.getRecords();
        List<DrivingLicenceRecord> list;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        if (searchDLIdentifiers.isPresent()) {
            list = dvlaRecords.stream()
                    .filter(rec -> searchDLIdentifiers
                            .map(si -> si.getIdValue().equalsIgnoreCase(rec.getDrivingLicenseNumber()))
                            .orElse(false))
                    .toList();
        } else {
            list = dvlaRecords.stream()
                    .filter(rec -> Optional.ofNullable(request.getSearchBioDetails().getLastName())
                            .map(f -> f.equalsIgnoreCase(rec.getLastName())).orElse(false))
                    .toList();
        }

        if (list.isEmpty()) {
            responseBuilder.matchStatus("No match found");
        } else if (list.size() == 1) {
            ICBMatch.ICBMatchBuilder matchBuilder = ICBMatch.builder();
            DrivingLicenceRecord record = list.getFirst();
            String lastNameMatched = "NO";
            String dvlaMatched = Optional.ofNullable(record.getDrivingLicenseNumber())
                    .map(dl -> dl.equalsIgnoreCase(searchDLIdentifiers
                            .map(SearchIdentifiers::getIdValue)
                            .orElse("")))
                    .map(b -> b ? "YES" : "NO")
                    .orElse("-");
            if(dvlaMatched.equals("NO")) {
                lastNameMatched = Optional.ofNullable(record.getLastName())
                        .map(mn -> mn.equalsIgnoreCase(request.getSearchBioDetails().getLastName()))
                        .map(b -> b ? "YES" : "NO")
                        .orElse("-");
            }

            matchBuilder.matches("No", lastNameMatched, "-", "No", "-", "-", dvlaMatched);
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
                                    .build()).toList());
        }
        return responseBuilder.searchComplete(true).build();
    }
    public List<DrivingLicenceRecord> searchByLastName(String lastName) {
        return recordLoader.getRecords().stream()
                .filter(record -> record.getLastName().equalsIgnoreCase(lastName))
                .collect(Collectors.toList());
    }

}
