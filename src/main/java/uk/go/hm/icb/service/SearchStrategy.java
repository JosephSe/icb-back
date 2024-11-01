package uk.go.hm.icb.service;

import uk.go.hm.icb.dto.ICBRequest;
import uk.go.hm.icb.dto.ICBResponse;

// Strategy interface and implementations
public interface SearchStrategy {
    ICBResponse search(ICBRequest request);
    long getDelay();
}