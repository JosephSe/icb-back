package uk.go.hm.icb.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ICBResponse {
    private final Boolean searchComplete;
    private final ICBMatch match;
    private final List<ICBMultiMatch> multiMatches;
    private List<String> verifications;
}
