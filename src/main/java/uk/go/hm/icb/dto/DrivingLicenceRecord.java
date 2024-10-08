package uk.go.hm.icb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DrivingLicenceRecord implements SearchDataType {
    private String drivingLicenceNumber;
    private String lastName;
    private String gender;
}