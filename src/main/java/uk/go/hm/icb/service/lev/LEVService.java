package uk.go.hm.icb.service.lev;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.go.hm.icb.dto.ICBMatch;
import uk.go.hm.icb.dto.ICBMultiMatch;
import uk.go.hm.icb.dto.ICBRequest;
import uk.go.hm.icb.dto.ICBResponse;
import uk.go.hm.icb.dto.LEVRecord;
import uk.go.hm.icb.dto.SearchBioDetails;
import uk.go.hm.icb.dto.SearchIDType;
import uk.go.hm.icb.dto.SearchIdentifiers;
import uk.go.hm.icb.dto.SearchSource;
import uk.go.hm.icb.service.SearchStrategy;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LEVService implements SearchStrategy {

    private final LevDataLoaderService dataLoaderService;

    private final long delay;

    @Autowired
    public LEVService(LevDataLoaderService dataLoaderService, @Value("${app.lev.delay}") long delay) {
        this.dataLoaderService = dataLoaderService;
        this.delay = delay;
    }

    public List<LEVRecord> searchByLastName(String lastName) {
        return dataLoaderService.getRecords().stream()
                .filter(record -> record.getLastName().equalsIgnoreCase(lastName))
                .collect(Collectors.toList());
    }

    /**
     * filter on LEV records last names
     */
    @Override
    public ICBResponse search(ICBRequest request) {
        ICBResponse.ICBResponseBuilder responseBuilder = ICBResponse.builder().searchSource(SearchSource.LEV);
        Optional<SearchIdentifiers> searchDLIdentifiers = Optional.ofNullable(request.getSearchIDTypes()).orElse(List.of())
                .stream().filter(t -> SearchIDType.BIRTH_CERTIFICATE == t.getIdType()).filter(t -> StringUtils.hasText(t.getIdValue()))
                .findFirst();
        List<LEVRecord> levRecords = dataLoaderService.getRecords();
        List<LEVRecord> list;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        if (searchDLIdentifiers.isPresent()) {
            list = levRecords.stream()
                    .filter(rec -> searchDLIdentifiers.map(si -> si.getIdValue().equalsIgnoreCase(rec.getBirthCertificate())).orElse(false))
                    .toList();
        } else {
            list = levRecords.stream()
                    .filter(rec -> Optional.ofNullable(request.getSearchBioDetails().getLastName())
                            .map(f -> f.equalsIgnoreCase(rec.getLastName())).orElse(false))
                    .filter(rec -> Optional.ofNullable(request.getSearchBioDetails().getFirstName())
                            .map(f -> f.equalsIgnoreCase(rec.getFirstName())).orElse(false))
                    .filter(rec -> LocalDate.parse(rec.getDateOfBirth(), formatter).isEqual(Optional.of(request.getSearchBioDetails()).map(SearchBioDetails::getDateOfBirth).orElse(LocalDate.now())))
                    .toList();
        }

        if (list.isEmpty()) {
            responseBuilder.matchStatus("No match found");
        } else if (list.size() == 1) {
            ICBMatch.ICBMatchBuilder matchBuilder = ICBMatch.builder();
            LEVRecord record = list.get(0);
            String levMatched = Optional.ofNullable(record.getBirthCertificate())
                    .map(dl -> dl.equalsIgnoreCase(searchDLIdentifiers
                            .map(SearchIdentifiers::getIdValue)
                            .orElse("")))
                    .map(b -> b ? "YES" : "NO")
                    .orElse("-");
            String firstNameMatched = Optional.ofNullable(record.getFirstName())
                    .map(mn -> mn.equalsIgnoreCase(request.getSearchBioDetails().getFirstName()))
                    .map(b -> b ? "YES" : "NO")
                    .orElse("-");
            String lastNameMatched = Optional.ofNullable(record.getLastName())
                    .map(mn -> mn.equalsIgnoreCase(request.getSearchBioDetails().getLastName()))
                    .map(b -> b ? "YES" : "NO")
                    .orElse("-");
            String middleNameMatched = Optional.ofNullable(record.getMiddleName())
                    .map(mn -> mn.equalsIgnoreCase(request.getSearchBioDetails().getMiddleName()))
                    .map(b -> b ? "YES" : "NO")
                    .orElse("-");
            String dobMatched = Optional.ofNullable(record.getDateOfBirth())
                    .map(mn -> LocalDate.parse(mn, formatter).isEqual(Optional.ofNullable(request.getSearchBioDetails()).map(SearchBioDetails::getDateOfBirth).orElse(LocalDate.now())))
                    .map(b -> b ? "YES" : "NO")
                    .orElse("-");

            matchBuilder.matches(firstNameMatched, lastNameMatched, middleNameMatched, dobMatched, "-", levMatched, "-");
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

    @Override
    public long getDelay() {
        return this.delay;
    }

}



