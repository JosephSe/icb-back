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
    private String fileName;
    // LEV details
    private String flag;
    private String motherName;
    private String motherMaidenName;
    private String motherPlaceOfBirth;
    private String fatherName;
    private String fatherPlaceOfBirth;
    private String registrationDistrict;
    private String subDistrict;
    private String administrativeArea;
    private LocalDate dateOfRegistration;
}