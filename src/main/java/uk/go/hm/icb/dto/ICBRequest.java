package uk.go.hm.icb.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ICBRequest {
    private final List<SearchSource> searchSources;
    private final List<SearchIdentifiers> searchIDTypes;
    private final SearchBioDetails searchBioDetails;
    private final SearchAddress searchAddress;
}
