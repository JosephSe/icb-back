package uk.go.hm.icb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SearchResponse <T extends SearchDataType> {
    private List<T> results;
}