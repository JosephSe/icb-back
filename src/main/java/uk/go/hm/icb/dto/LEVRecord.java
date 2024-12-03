package uk.go.hm.icb.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@Builder
@EqualsAndHashCode
public class LEVRecord implements SearchDataType {
    private String firstName;
    private String middleName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String address;
    private String birthCertificate;
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

    public LEVRecord(String firstName, String middleName, String lastName, LocalDate dateOfBirth, String address, String birthCertificate, String flag, String motherName, String motherMaidenName, String motherPlaceOfBirth, String fatherName, String fatherPlaceOfBirth, String registrationDistrict, String subDistrict, String administrativeArea, LocalDate dateOfRegistration) {
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.birthCertificate = birthCertificate;
        this.flag = flag;
        this.motherName = motherName;
        this.motherMaidenName = motherMaidenName;
        this.motherPlaceOfBirth = motherPlaceOfBirth;
        this.fatherName = fatherName;
        this.fatherPlaceOfBirth = fatherPlaceOfBirth;
        this.registrationDistrict = registrationDistrict;
        this.subDistrict = subDistrict;
        this.administrativeArea = administrativeArea;
        this.dateOfRegistration = dateOfRegistration;
    }
}