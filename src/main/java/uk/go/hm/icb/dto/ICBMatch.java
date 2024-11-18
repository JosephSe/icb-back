package uk.go.hm.icb.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.util.Pair;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

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
                                       String drivingLicenseNumberMatched, String passportNumber) {
            matches = new ArrayList<>(List.of(
                    Pair.of("First Name", firstNameMatched),
                    Pair.of("Last Name", lastNameMatched),
                    Pair.of("Middle Name", middleNameMatched),
                    Pair.of("Date of Birth", dateOfBirthMatched),
                    Pair.of("Address", addressMatched),
                    Pair.of("Unique Identifier - Birth Cert", birthCertificateMatched),
                    Pair.of("Driving Licence Number", drivingLicenseNumberMatched),
                    Pair.of("Passport Number", passportNumber)
            ));
            return this;
        }
        public ICBMatchBuilder matches(String firstNameMatched, String lastNameMatched, String middleNameMatched,
                                       String dateOfBirthMatched, String addressMatched, String birthCertificateMatched,
                                       String drivingLicenseNumberMatched, String passportNumber, String flag) {
            ICBMatchBuilder builder = matches(firstNameMatched, lastNameMatched, middleNameMatched, dateOfBirthMatched, addressMatched, birthCertificateMatched, drivingLicenseNumberMatched, passportNumber);
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
    }
}
