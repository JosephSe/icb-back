package com.example.icb.service;

public abstract class AbstractSearchService {
    protected boolean isValidSearchRequest(ICBRequest request) {
        return request != null && request.getSearchBioDetails() != null;
    }

    protected ICBResponse.ICBResponseBuilder createBaseResponseBuilder(SearchSource source) {
        return ICBResponse.builder()
                .searchSource(source)
                .searchComplete(true);
    }
}
