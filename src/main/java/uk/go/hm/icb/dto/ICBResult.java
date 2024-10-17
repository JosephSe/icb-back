package uk.go.hm.icb.dto;

import lombok.Data;

import java.util.List;

@Data
public class ICBResult {
    private final SearchSource searchSource;
    private final List<String> verifications;
    private final String firstName;
    private final String middleName;
    private final String lastName;
    private final String dateOfBirth;
    private final String address;
    private final String birthCertificate;
    private final String drivingLicenseNumber;
}
