package uk.go.hm.icb.dto;

import lombok.Data;

@Data
public class SearchIdentifiers {
    private SearchSource searchSource;
    private SearchIDType idType;
    private String idValue;
}
