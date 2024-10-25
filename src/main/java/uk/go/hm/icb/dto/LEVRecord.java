package uk.go.hm.icb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class LEVRecord extends ICBGenericRecord implements SearchDataType {
    private String birthCertificate;

    public LEVRecord(String firstName, String middleName, String lastName, String dateOfBirth, String address, String birthCertificate) {
        super(firstName, middleName, lastName, dateOfBirth, address);
        this.birthCertificate = birthCertificate;
    }
}