package uk.go.hm.icb.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ICBMultiMatch {
    private String firstName;
    private String middleName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String address;
    private String drivingLicenseNumber;
    private String birthCertificate;
    private String passportNumber;
}
