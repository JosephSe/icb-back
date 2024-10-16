package uk.go.hm.icb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LEVRecord implements SearchDataType {
    private String id;
    private String lastName;
    private String firstName;
}