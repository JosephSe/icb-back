package uk.go.hm.icb.service.ipcs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.go.hm.icb.dto.*;
import uk.go.hm.icb.service.AbstractSearchService;

import java.util.List;
import java.util.Optional;

@Service
public class IPCSService extends AbstractSearchService {

    private final IPCSDataLoader recordLoader;

    @Autowired
    public IPCSService(IPCSDataLoader recordLoader, @Value("${app.delay.ipcs}") long delay) {
        super(delay);
        this.recordLoader = recordLoader;
    }

    @Override
    public ICBResponse search(ICBRequest request) {
        ICBResponse.ICBResponseBuilder responseBuilder = ICBResponse.builder()
                .searchSource(SearchSource.IPCS);

        List<IPCSRecord> matchedRecords = findMatchingRecords(request);

        return buildBaseResponse(responseBuilder, matchedRecords, request, new IPCSMatchResponseBuilder());
    }

    private List<IPCSRecord> findMatchingRecords(ICBRequest request) {
        Optional<SearchIdentifiers> passportIdentifier = findIdentifierByType(request, SearchIDType.IPCS_PPT_NUM);
        List<IPCSRecord> records = recordLoader.getRecords();

        return records.stream()
                .filter(record -> matchesBioDetails(record, request.getSearchBioDetails()))
                .filter(record -> matchesPassport(record, passportIdentifier))
                .toList();
    }

    private boolean matchesBioDetails(IPCSRecord record, SearchBioDetails bioDetails) {
        return Optional.ofNullable(bioDetails.getFirstName())
                .map(firstName -> firstName.equalsIgnoreCase(record.getFirstName()))
                .orElse(false)
                &&
                Optional.ofNullable(bioDetails.getLastName())
                .map(lastName -> lastName.equalsIgnoreCase(record.getLastName()))
                .orElse(false);
    }

    private boolean matchesPassport(IPCSRecord record, Optional<SearchIdentifiers> passportIdentifier) {
        return passportIdentifier
                .map(identifier -> identifier.getIdValue().equalsIgnoreCase(record.getPassportNumber()))
                .orElse(false);
    }

    private class IPCSMatchResponseBuilder implements MatchResponseBuilder {
        @Override
        public ICBResponse buildSingleMatchResponse(ICBResponse.ICBResponseBuilder responseBuilder,
                                                  Object record,
                                                  ICBRequest request) {
            IPCSRecord ipcsRecord = (IPCSRecord) record;
            SearchBioDetails bioDetails = request.getSearchBioDetails();

            ICBMatch match = ICBMatch.builder()
                    .matches(
                            matchField(bioDetails.getFirstName(), ipcsRecord.getFirstName()),
                            matchField(bioDetails.getLastName(), ipcsRecord.getLastName()),
                            "-",
                            "-",
                            "-",
                            null,
                            null,
                            matchField(
                                findIdentifierByType(request, SearchIDType.IPCS_PPT_NUM)
                                    .map(SearchIdentifiers::getIdValue)
                                    .orElse(null),
                                ipcsRecord.getPassportNumber()),
                            null

                    )
                    .verification("Match 100%")
                    .build();

            return responseBuilder
                    .matchStatus("One match found")
                    .match(match)
                    .searchComplete(true)
                    .build();
        }

        @Override
        public ICBResponse buildMultipleMatchResponse(ICBResponse.ICBResponseBuilder responseBuilder,
                                                    List<?> matches) {
            List<IPCSRecord> ipcsMatches = (List<IPCSRecord>) matches;
            List<ICBMultiMatch> multiMatches = ipcsMatches.stream()
                    .map(this::createMultiMatch)
                    .toList();

            return responseBuilder
                    .matchStatus("Multiple matches found")
                    .multiMatches(multiMatches)
                    .searchComplete(true)
                    .build();
        }

        private ICBMultiMatch createMultiMatch(IPCSRecord record) {
            return ICBMultiMatch.builder()
                    .firstName(record.getFirstName())
                    .lastName(record.getLastName())
                    .passportNumber(record.getPassportNumber())
                    .build();
        }
    }
}