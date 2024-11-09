package uk.go.hm.icb.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class IPCSRecord implements SearchDataType {
    private String firstName;
    private String lastName;
    private String passportNumber;

    public IPCSRecord(String firstName, String lastName, String passportNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.passportNumber = passportNumber;
    }
}