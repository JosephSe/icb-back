package uk.go.hm.icb.service.ipcs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.go.hm.icb.dto.DrivingLicenceRecord;
import uk.go.hm.icb.dto.ICBMatch;
import uk.go.hm.icb.dto.ICBMultiMatch;
import uk.go.hm.icb.dto.ICBRequest;
import uk.go.hm.icb.dto.ICBResponse;
import uk.go.hm.icb.dto.IPCSRecord;
import uk.go.hm.icb.dto.SearchBioDetails;
import uk.go.hm.icb.dto.SearchIDType;
import uk.go.hm.icb.dto.SearchIdentifiers;
import uk.go.hm.icb.dto.SearchSource;
import uk.go.hm.icb.service.SearchStrategy;
import uk.go.hm.icb.service.dvla.DVLADataLoader;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class IPCSService implements SearchStrategy {

    private final IPCSDataLoader recordLoader;

    private final long delay;

    @Autowired
    public IPCSService(IPCSDataLoader recordLoader, @Value("${app.ipcs.delay}") long delay) {
        this.recordLoader = recordLoader;
        this.delay = delay;
    }

    /**
     * filter on DVLA records first on DL Number, then on first, last and middle (if it exists) names
     * filtering on other fields should also be added if they are in the input request
     * */
    @Override
    public ICBResponse search(ICBRequest request) {
        ICBResponse.ICBResponseBuilder responseBuilder = ICBResponse.builder().searchSource(SearchSource.IPCS);
        Optional<SearchIdentifiers> searchDLIdentifiers = Optional.ofNullable(request.getSearchIDTypes()).orElse(List.of())
                .stream().filter(t -> SearchIDType.IPCS_PPT_NUM == t.getIdType()).filter(t -> StringUtils.hasText(t.getIdValue()))
                .findFirst();

        List<IPCSRecord> ipcsRecords = recordLoader.getRecords();

        String firstName = request.getSearchBioDetails().getFirstName();
        String lastName = request.getSearchBioDetails().getLastName();
        String passportNumber = searchDLIdentifiers.get().getIdValue();

        Predicate<IPCSRecord> firstNamePredicate = record ->
                request.getSearchBioDetails().getFirstName() == null || firstName.equals(record.getFirstName());

        Predicate<IPCSRecord> lastNamePredicate = record ->
                lastName == null || lastName.equals(record.getLastName());

        Predicate<IPCSRecord> passportPredicate = record ->
                passportNumber == null || passportNumber.equals(record.getPassportNumber());

        List<IPCSRecord> list = ipcsRecords.stream()
                .filter(firstNamePredicate)
                .filter(lastNamePredicate)
                .filter(passportPredicate)
                .toList();

        if (list.isEmpty()) {
            responseBuilder.matchStatus("No match found");
        } else if (list.size() == 1) {
            ICBMatch.ICBMatchBuilder matchBuilder = ICBMatch.builder();
            IPCSRecord record = list.getFirst();
            String ipcsMatched = "YES";
            String firstNameMatched = "YES";
            String lastNameMatched = "YES";

            matchBuilder.matches(firstNameMatched, lastNameMatched, "-", "-", "-", "-", "-",ipcsMatched);
            responseBuilder.matchStatus("One match found").match(matchBuilder.build());
        } else {
            responseBuilder.matchStatus("Multiple matches found")
                    .multiMatches(
                            list.stream().map(rec -> ICBMultiMatch.builder()
                                    .firstName(rec.getFirstName())
                                    .lastName(rec.getLastName())
                                    .drivingLicenseNumber(rec.getPassportNumber())
                                    .build()).toList());
        }

        return responseBuilder.searchComplete(true).build();
    }

    @Override
    public long getDelay() {
        return delay;
    }

}
