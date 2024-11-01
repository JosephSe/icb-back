package uk.go.hm.icb.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SearchIdentifiers {
    private SearchSource searchSource;
    private SearchIDType idType;
    private String idValue;
}
