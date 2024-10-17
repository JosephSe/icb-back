package uk.go.hm.icb.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@NoArgsConstructor
@ToString
public class ICBResponse {
    private String firstName;
    private String middleName;
    private String lastName;
    private String dateOfBirth;
    private String address;
    private SearchSource searchSource;
    private List<String> verifications;
    private String birthCertificate;
    private String drivingLicenseNumber;

   public ICBResponse(DrivingLicenceRecord licenceRecord) {
        this.firstName = licenceRecord.getFirstName();
        this.middleName = licenceRecord.getMiddleName();
        this.lastName = licenceRecord.getLastName();
        this.dateOfBirth = licenceRecord.getDateOfBirth();
        this.address = licenceRecord.getAddress();
        this.drivingLicenseNumber = licenceRecord.getDrivingLicenseNumber();
        this.searchSource = SearchSource.DVLA;
    }
}
