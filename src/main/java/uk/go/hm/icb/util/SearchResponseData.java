package uk.go.hm.icb.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import uk.go.hm.icb.dto.ICBResponse;
import uk.go.hm.icb.dto.SearchSource;

@Data
@AllArgsConstructor
public class SearchResponseData {
    private SearchSource source;
    private ICBResponse response;
    private long delay;
}