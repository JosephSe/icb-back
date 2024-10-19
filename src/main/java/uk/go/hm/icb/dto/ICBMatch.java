package uk.go.hm.icb.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ICBMatch {
    private String firstNameMatched;
    private String middleNameMatched;
    private String lastNameMatched;
    private String dateOfBirthMatched;
    private String addressMatched;
    private String birthCertificateMatched;
    private String drivingLicenseNumberMatched;

}
