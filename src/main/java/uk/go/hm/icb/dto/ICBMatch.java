package uk.go.hm.icb.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.util.Pair;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@Builder
public class ICBMatch {
    private List<Pair<String, String>> matches;
    private List<String> verifications;
    private boolean isFullRecordAvailable;
    private ICBMatchRecord icbMatchRecord;

    public static class ICBMatchBuilder {
        public ICBMatchBuilder matches(String firstNameMatched, String lastNameMatched, String middleNameMatched,
                                       String dateOfBirthMatched, String addressMatched, String birthCertificateMatched,
                                       String drivingLicenseNumberMatched, String passportNumber, String flag) {
            matches = new ArrayList<>(List.of(
                    Pair.of("First Name", firstNameMatched),
                    Pair.of("Last Name", lastNameMatched),
                    Pair.of("Middle Name", middleNameMatched),
                    Pair.of("Date of Birth", dateOfBirthMatched),
                    Pair.of("Address", addressMatched)
            ));
            if (StringUtils.hasText(birthCertificateMatched)) {
                matches.add(Pair.of("Unique Identifier - Birth Cert", birthCertificateMatched));
            }
            if (StringUtils.hasText(drivingLicenseNumberMatched)) {
                    matches.add(Pair.of("Driving Licence Number", drivingLicenseNumberMatched));
            }
            if (StringUtils.hasText(passportNumber)) {
                    matches.add(Pair.of("Passport Number", passportNumber));
            }
            if (StringUtils.hasText(flag)) {
                matches.add(Pair.of("Flag", flag));
            }
            return this;
        }

        public ICBMatchBuilder verification(String verification) {
            if (verifications == null) {
                verifications = new ArrayList<>();
            }
            verifications.add(verification);
            return this;
        }
        public ICBMatchBuilder addMatch(String key, String value) {
            if (matches == null) {
                matches = new ArrayList<>();
            }
            matches.add(Pair.of(key, value));
            return this;
        }
    }
}
