package uk.go.hm.icb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ICBGenericRecord {
    private String firstName;
    private String middleName;
    private String lastName;
    private String dateOfBirth;
    private String address;
}
