package uk.go.hm.icb.dto;

import lombok.Data;

@Data
public class SearchIdentifiers {
    private SearchSource searchSource;
    private SearchIDType searchIDType;
    private String value;
}
