package uk.go.hm.icb.service.dvla;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.go.hm.icb.dto.*;
import uk.go.hm.icb.service.AbstractSearchService;

import java.util.List;
import java.util.Optional;

@Service
public class DVLAService extends AbstractSearchService {

    private final DVLADataLoader recordLoader;

    @Autowired
    public DVLAService(DVLADataLoader recordLoader, @Value("${app.dvla.delay}") long delay) {
        super(delay);
        this.recordLoader = recordLoader;
    }

    @Override
    public ICBResponse search(ICBRequest request) {
        ICBResponse.ICBResponseBuilder responseBuilder = ICBResponse.builder()
                .searchSource(SearchSource.DVLA);

        List<DrivingLicenceRecord> matchedRecords = findMatchingRecords(request);

        return buildBaseResponse(responseBuilder, matchedRecords, request, new DVLAMatchResponseBuilder());
    }

    private List<DrivingLicenceRecord> findMatchingRecords(ICBRequest request) {
        Optional<SearchIdentifiers> driverLicenseIdentifier = findIdentifierByType(request, SearchIDType.DRIVER_LICENSE);
        List<DrivingLicenceRecord> records = recordLoader.getRecords();

        if (driverLicenseIdentifier.isPresent()) {
            List<DrivingLicenceRecord> licenseMatches = filterByLicenseNumber(records, driverLicenseIdentifier.get());
            if (!licenseMatches.isEmpty()) {
                return licenseMatches;
            }
        }

        return filterByLastName(records, request.getSearchBioDetails());
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

    private String matchDriverLicense(ICBRequest request, DrivingLicenceRecord record) {
        return findIdentifierByType(request, SearchIDType.DRIVER_LICENSE)
                .map(SearchIdentifiers::getIdValue)
                .map(dl -> dl.equalsIgnoreCase(record.getDrivingLicenseNumber()))
                .map(matches -> matches ? "YES" : "NO")
                .orElse("-");
    }

    private class DVLAMatchResponseBuilder implements MatchResponseBuilder {
        @Override
        public ICBResponse buildSingleMatchResponse(ICBResponse.ICBResponseBuilder responseBuilder,
                                                  Object record,
                                                  ICBRequest request) {
            DrivingLicenceRecord dlRecord = (DrivingLicenceRecord) record;
            SearchBioDetails bioDetails = request.getSearchBioDetails();

            ICBMatch match = ICBMatch.builder()
                    .matches(
                            matchField(bioDetails.getFirstName(), dlRecord.getFirstName()),
                            matchField(bioDetails.getLastName(), dlRecord.getLastName()),
                            matchField(bioDetails.getMiddleName(), dlRecord.getMiddleName()),
                            matchDateField(bioDetails.getDateOfBirth(), dlRecord.getDateOfBirth()),
                            "-",
                            "-",
                            matchDriverLicense(request, dlRecord),
                            "-"
                    )
                    .build();

            return responseBuilder.matchStatus("One match found")
                    .match(match)
                    .searchComplete(true)
                    .build();
        }

        @Override
        @SuppressWarnings("unchecked")
        public ICBResponse buildMultipleMatchResponse(ICBResponse.ICBResponseBuilder responseBuilder,
                                                    List<?> matches) {
            List<DrivingLicenceRecord> dlMatches = (List<DrivingLicenceRecord>) matches;
            List<ICBMultiMatch> multiMatches = dlMatches.stream()
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
    }
}
