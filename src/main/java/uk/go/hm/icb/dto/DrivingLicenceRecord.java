package uk.go.hm.icb.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper=false)
public class DrivingLicenceRecord implements SearchDataType {
    private String firstName;
    private String middleName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String address;
    private String drivingLicenseNumber;
    private String photoBase64;

    public DrivingLicenceRecord(String firstName, String middleName, String lastName, LocalDate dateOfBirth, String address, String drivingLicenseNumber) {
//        super(firstName, middleName, lastName, dateOfBirth, address);
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.drivingLicenseNumber = drivingLicenseNumber;
    }
}