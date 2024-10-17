package uk.go.hm.icb.dto;

import lombok.Data;

import java.util.List;

@Data
public class ICBRequest {
    private final List<SearchSource> searchSources;
    private final List<SearchIDType> searchIDTypes;
    private final SearchBioDetails searchBioDetails;
    private final SearchAddress searchAddress;
}
