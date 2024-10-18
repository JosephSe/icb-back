package uk.go.hm.icb.dto;

import lombok.Data;

@Data
public class SearchAddress {
    private String line1;
    private String line2;
    private String city;
    private String postCode;
}
