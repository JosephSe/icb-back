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

    public LEVRecord(String firstName, String middleName, String lastName, LocalDate dateOfBirth, String address, String birthCertificate, String flag) {
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.birthCertificate = birthCertificate;
        this.flag = flag;
    }
}