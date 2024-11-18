package uk.go.hm.icb.dto;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@Builder
@EqualsAndHashCode(callSuper=false)
public class ICBMatchRecord implements SearchDataType {
    private String firstName;
    private String middleName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String address;
    private String drivingLicenseNumber;
    private String passportNumber;
    private String birthCertificate;
    private String photo;
}