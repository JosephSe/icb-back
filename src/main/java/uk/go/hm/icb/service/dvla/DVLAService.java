package uk.go.hm.icb.service.dvla;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.go.hm.icb.dto.*;
import uk.go.hm.icb.service.SearchStrategy;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DVLAService implements SearchStrategy {

    private final DVLADataLoader recordLoader;
    private final long delay;

    @Autowired
    public DVLAService(DVLADataLoader recordLoader, @Value("${app.dvla.delay}") long delay) {
        this.recordLoader = recordLoader;
        this.delay = delay;
    }

    @Override
    public ICBResponse search(ICBRequest request) {
        ICBResponse.ICBResponseBuilder responseBuilder = ICBResponse.builder()
                .searchSource(SearchSource.DVLA);

        List<DrivingLicenceRecord> matchedRecords = findMatchingRecords(request);

        return buildResponse(responseBuilder, matchedRecords, request);
    }

    private List<DrivingLicenceRecord> findMatchingRecords(ICBRequest request) {
        Optional<SearchIdentifiers> driverLicenseIdentifier = findDriverLicenseIdentifier(request);
        List<DrivingLicenceRecord> records = recordLoader.getRecords();

        if (driverLicenseIdentifier.isPresent()) {
            List<DrivingLicenceRecord> licenseMatches = filterByLicenseNumber(records, driverLicenseIdentifier.get());
            if (!licenseMatches.isEmpty()) {
                return licenseMatches;
            }
        }

        return filterByLastName(records, request.getSearchBioDetails());
    }

    private Optional<SearchIdentifiers> findDriverLicenseIdentifier(ICBRequest request) {
        return Optional.ofNullable(request.getSearchIDTypes())
                .orElse(List.of())
                .stream()
                .filter(t -> SearchIDType.DRIVER_LICENSE == t.getIdType())
                .filter(t -> StringUtils.hasText(t.getIdValue()))
                .findFirst();
    }

    private List<DrivingLicenceRecord> filterByLicenseNumber(List<DrivingLicenceRecord> records,
                                                             SearchIdentifiers identifier) {
        return records.stream()
                .filter(rec -> identifier.getIdValue().equalsIgnoreCase(rec.getDrivingLicenseNumber()))
                .toList();
    }

    private List<DrivingLicenceRecord> filterByLastName(List<DrivingLicenceRecord> records,
                                                        SearchBioDetails bioDetails) {
        return records.stream()
                .filter(rec -> Optional.ofNullable(bioDetails.getLastName())
                        .map(lastName -> lastName.equalsIgnoreCase(rec.getLastName()))
                        .orElse(false))
                .toList();
    }

    private ICBResponse buildResponse(ICBResponse.ICBResponseBuilder responseBuilder,
                                      List<DrivingLicenceRecord> matches,
                                      ICBRequest request) {
        if (matches.isEmpty()) {
            return responseBuilder.matchStatus("No match found")
                    .searchComplete(true)
                    .build();
        } else if (matches.size() == 1) {
            return buildSingleMatchResponse(responseBuilder, matches.getFirst(), request);
        } else {
            return buildMultipleMatchResponse(responseBuilder, matches);
        }
    }

    private ICBResponse buildSingleMatchResponse(ICBResponse.ICBResponseBuilder responseBuilder,
                                                 DrivingLicenceRecord record,
                                                 ICBRequest request) {
        ICBMatch.ICBMatchBuilder matchBuilder = ICBMatch.builder();
        SearchBioDetails bioDetails = request.getSearchBioDetails();

        // Create individual match results for each field
        String firstNameMatch = matchField(bioDetails.getFirstName(), record.getFirstName());
        String lastNameMatch = matchField(bioDetails.getLastName(), record.getLastName());
        String middleNameMatch = matchField(bioDetails.getMiddleName(), record.getMiddleName());
        String dobMatch = matchDateField(bioDetails.getDateOfBirth(), record.getDateOfBirth());
        String addressMatch = "-";
        String postcodeMatch = "-";
        String driverLicenseMatch = matchDriverLicense(request, record);
        String additionalInfoMatch = "-";

        return responseBuilder.matchStatus("One match found")
                .match(matchBuilder.matches(firstNameMatch, lastNameMatch, middleNameMatch, dobMatch, addressMatch, postcodeMatch, driverLicenseMatch, additionalInfoMatch)
                        .build())
                .searchComplete(true)
                .build();
    }

    private ICBResponse buildMultipleMatchResponse(ICBResponse.ICBResponseBuilder responseBuilder,
                                                   List<DrivingLicenceRecord> matches) {
        List<ICBMultiMatch> multiMatches = matches.stream()
                .map(this::createMultiMatch)
                .toList();

        return responseBuilder.matchStatus("Multiple matches found")
                .multiMatches(multiMatches)
                .searchComplete(true)
                .build();
    }

    private ICBMultiMatch createMultiMatch(DrivingLicenceRecord record) {
        return ICBMultiMatch.builder()
                .firstName(record.getFirstName())
                .lastName(record.getLastName())
                .middleName(record.getMiddleName())
                .dateOfBirth(record.getDateOfBirth())
                .address(record.getAddress())
                .drivingLicenseNumber(record.getDrivingLicenseNumber())
                .build();
    }

    private String matchField(String requestValue, String recordValue) {
        return Optional.ofNullable(requestValue)
                .filter(StringUtils::hasText)
                .map(value -> value.equalsIgnoreCase(recordValue) ? "YES" : "NO")
                .orElse("-");
    }

    private String matchDateField(LocalDate requestDate, LocalDate recordDate) {
        return Optional.ofNullable(recordDate)
                .map(date -> date.isEqual(Optional.ofNullable(requestDate).orElse(LocalDate.now())))
                .map(matches -> matches ? "YES" : "NO")
                .orElse("-");
    }

    private String matchDriverLicense(ICBRequest request, DrivingLicenceRecord record) {
        return findDriverLicenseIdentifier(request)
                .map(SearchIdentifiers::getIdValue)
                .map(dl -> dl.equalsIgnoreCase(record.getDrivingLicenseNumber()))
                .map(matches -> matches ? "YES" : "NO")
                .orElse("-");
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
