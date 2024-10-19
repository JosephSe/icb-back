package uk.go.hm.icb.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ICBMultiMatch {
    private String firstName;
    private String middleName;
    private String lastName;
    private String dateOfBirth;
    private String address;
}
