package uk.go.hm.icb.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder(toBuilder = true)
public class ICBResponse {
    private final SearchSource searchSource;
    private final Boolean searchComplete;
    private final String matchStatus;
    private final ICBMatch match;
    private final List<ICBMultiMatch> multiMatches;
}
