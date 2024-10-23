package uk.go.hm.icb.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.util.Pair;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class ICBMatch {
    private List<Pair<String, String>> matches;
    private List<String> verifications;

    public static class ICBMatchBuilder {
        public ICBMatchBuilder matches(String firstNameMatched, String lastNameMatched, String middleNameMatched, String dateOfBirthMatched, String addressMatched, String birthCertificateMatched, String drivingLicenseNumberMatched) {
            matches = List.of(
                    Pair.of("First Name", firstNameMatched),
                    Pair.of("Last Name", lastNameMatched),
                    Pair.of("Middle Name", middleNameMatched),
                    Pair.of("Date of Birth", dateOfBirthMatched),
                    Pair.of("Address", addressMatched),
                    Pair.of("Unique Identifier - Birth Cert", birthCertificateMatched),
                    Pair.of("Driving Licence Number", drivingLicenseNumberMatched)
            );
            return this;
        }

        public ICBMatchBuilder verification(String verification) {
            if (verifications == null) {
                verifications = new ArrayList<>();
            }
            verifications.add(verification);
            return this;
        }
    }
}
