package uk.go.hm.icb.service.lev;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.go.hm.icb.dto.*;
import uk.go.hm.icb.service.AbstractSearchService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class LEVService extends AbstractSearchService {

    private final LEVDataLoader dataLoaderService;

    @Autowired
    public LEVService(LEVDataLoader dataLoaderService, @Value("${app.lev.delay}") long delay) {
        super(delay);
        this.dataLoaderService = dataLoaderService;
    }

    @Override
    public ICBResponse search(ICBRequest request) {
        ICBResponse.ICBResponseBuilder responseBuilder = ICBResponse.builder()
                .searchSource(SearchSource.LEV);

        List<LEVRecord> matchedRecords = findMatchingRecords(request);

        return buildBaseResponse(responseBuilder, matchedRecords, request, new LEVMatchResponseBuilder());
    }

    private List<LEVRecord> findMatchingRecords(ICBRequest request) {
        Optional<SearchIdentifiers> birthCertIdentifier = findIdentifierByType(request, SearchIDType.BIRTH_CERTIFICATE);
        List<LEVRecord> levRecords = dataLoaderService.getRecords();

        if (birthCertIdentifier.isPresent()) {
            List<LEVRecord> certMatches = filterByBirthCertificate(levRecords, birthCertIdentifier.get());
            if (!certMatches.isEmpty()) {
                return certMatches;
            }
        }

        return filterByBioDetails(levRecords, request.getSearchBioDetails());
    }

    private List<LEVRecord> filterByBirthCertificate(List<LEVRecord> records, SearchIdentifiers identifier) {
        return records.stream()
                .filter(rec -> identifier.getIdValue().equalsIgnoreCase(rec.getBirthCertificate()))
                .toList();
    }

    private List<LEVRecord> filterByBioDetails(List<LEVRecord> records, SearchBioDetails bioDetails) {
        return records.stream()
                .filter(rec -> Optional.ofNullable(bioDetails.getLastName())
                        .map(f -> f.equalsIgnoreCase(rec.getLastName()))
                        .orElse(false))
                .filter(rec -> Optional.ofNullable(bioDetails.getFirstName())
                        .map(f -> f.equalsIgnoreCase(rec.getFirstName()))
                        .orElse(false))
                .filter(rec -> rec.getDateOfBirth().isEqual(
                        Optional.of(bioDetails)
                                .map(SearchBioDetails::getDateOfBirth)
                                .orElse(LocalDate.now())))
                .toList();
    }

    private class LEVMatchResponseBuilder implements MatchResponseBuilder {
        @Override
        public ICBResponse buildSingleMatchResponse(ICBResponse.ICBResponseBuilder responseBuilder,
                                                  Object record,
                                                  ICBRequest request) {
            LEVRecord levRecord = (LEVRecord) record;
            SearchBioDetails bioDetails = request.getSearchBioDetails();

            String birthCertMatch = findIdentifierByType(request, SearchIDType.BIRTH_CERTIFICATE)
                    .map(SearchIdentifiers::getIdValue)
                    .map(cert -> cert.equalsIgnoreCase(levRecord.getBirthCertificate()))
                    .map(matches -> matches ? "YES" : "NO")
                    .orElse("-");

            ICBMatch match = ICBMatch.builder()
                    .matches(
                            matchField(bioDetails.getFirstName(), levRecord.getFirstName()),
                            matchField(bioDetails.getLastName(), levRecord.getLastName()),
                            matchField(bioDetails.getMiddleName(), levRecord.getMiddleName()),
                            matchDateField(bioDetails.getDateOfBirth(), levRecord.getDateOfBirth()),
                            "-",
                            birthCertMatch,
                            "-",
                            "-",
                            levRecord.getFlag()
                    )
                    .build();

            return responseBuilder.matchStatus("One match found")
                    .match(match)
                    .searchComplete(true)
                    .build();
        }

        @Override
        public ICBResponse buildMultipleMatchResponse(ICBResponse.ICBResponseBuilder responseBuilder,
                                                    List<?> matches) {
            List<LEVRecord> levMatches = (List<LEVRecord>) matches;
            List<ICBMultiMatch> multiMatches = levMatches.stream()
                    .map(this::createMultiMatch)
                    .toList();

            return responseBuilder.matchStatus("Multiple matches found")
                    .multiMatches(multiMatches)
                    .searchComplete(true)
                    .build();
        }

        private ICBMultiMatch createMultiMatch(LEVRecord record) {
            return ICBMultiMatch.builder()
                    .firstName(record.getFirstName())
                    .lastName(record.getLastName())
                    .middleName(record.getMiddleName())
                    .dateOfBirth(record.getDateOfBirth())
                    .address(record.getAddress())
                    .birthCertificate(record.getBirthCertificate())
                    .flag(record.getFlag())
                    .build();
        }
    }
}
