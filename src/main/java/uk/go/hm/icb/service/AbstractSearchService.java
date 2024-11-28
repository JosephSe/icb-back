package uk.go.hm.icb.service;

import org.springframework.util.StringUtils;
import uk.go.hm.icb.dto.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;

public abstract class AbstractSearchService implements SearchStrategy {
    private final long delay;

    protected AbstractSearchService(long delay) {
        this.delay = delay;
    }

    @Override
    public long getDelay() {
        return delay;
    }

    protected ICBResponse buildBaseResponse(ICBResponse.ICBResponseBuilder responseBuilder, List<?> matches, ICBRequest request, MatchResponseBuilder matchResponseBuilder) {
        if (matches.isEmpty()) {
            return buildNoMatchResponse(responseBuilder);
        } else if (matches.size() == 1) {
            return matchResponseBuilder.buildSingleMatchResponse(responseBuilder, matches.get(0), request);
        } else {
            return matchResponseBuilder.buildMultipleMatchResponse(responseBuilder, matches);
        }
    }

    protected ICBResponse buildNoMatchResponse(ICBResponse.ICBResponseBuilder responseBuilder) {
        return responseBuilder
                .matchStatus("No match found")
                .searchComplete(true)
                .build();
    }

    protected String matchField(String requestValue, String recordValue) {
        return Optional.ofNullable(requestValue)
                .filter(StringUtils::hasText)
                .map(value -> value.equalsIgnoreCase(recordValue) ? "YES" : "NO")
                .orElse("-");
    }

    protected String matchDateField(LocalDate requestDate, LocalDate recordDate) {
        return Optional.ofNullable(requestDate)
                .map(date -> date.isEqual(Optional.ofNullable(recordDate).orElse(LocalDate.now())))
                .map(matches -> matches ? "YES" : "NO")
                .orElse("-");
    }

    protected boolean matchesIdentifier(Object record, ICBRequest request, SearchIDType idType, BiPredicate<Object, String> matcher) {
        return findIdentifierByType(request, idType)
                .map(identifier -> matcher.test(record, identifier.getIdValue()))
                .orElse(false);
    }

    protected Optional<SearchIdentifiers> findIdentifierByType(ICBRequest request, SearchIDType idType) {
        return Optional.ofNullable(request.getSearchIDTypes())
                .orElse(List.of())
                .stream()
                .filter(t -> idType == t.getIdType())
                .filter(t -> StringUtils.hasText(t.getIdValue()))
                .findFirst();
    }

    protected interface MatchResponseBuilder {
        ICBResponse buildSingleMatchResponse(ICBResponse.ICBResponseBuilder responseBuilder, Object record, ICBRequest request);

        ICBResponse buildMultipleMatchResponse(ICBResponse.ICBResponseBuilder responseBuilder, List<?> matches);
    }
} 