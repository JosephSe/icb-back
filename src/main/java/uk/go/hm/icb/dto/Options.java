package uk.go.hm.icb.dto;

import lombok.Data;

@Data
class Options {
    private String lastNameMatchType = "exact";
    private String firstNamesMatchType = "phonetic";
    private boolean searchNamesOnPreviousLicences = true;
    private boolean searchDateOfBirthOnPreviousLicences = true;
    private boolean includeImagesInResults = false;
    private boolean includePartialPostcodesInResults = true;
    private String orderBy = "relevance";
}