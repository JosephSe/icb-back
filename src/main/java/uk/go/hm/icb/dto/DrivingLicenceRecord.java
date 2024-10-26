package uk.go.hm.icb.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class DrivingLicenceRecord extends ICBGenericRecord implements SearchDataType {
    private String drivingLicenseNumber;

    public DrivingLicenceRecord(String firstName, String middleName, String lastName, String dateOfBirth, String address, String drivingLicenseNumber) {
        super(firstName, middleName, lastName, dateOfBirth, address);
        this.drivingLicenseNumber = drivingLicenseNumber;
    }
}