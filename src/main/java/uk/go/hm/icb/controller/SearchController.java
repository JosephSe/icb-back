package uk.go.hm.icb.controller;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.go.hm.icb.dto.SearchRequest;
import uk.go.hm.icb.dto.SearchResponse;
import uk.go.hm.icb.service.LEVService;

import java.util.logging.Level;

@Log
@RestController
@RequestMapping("/api")
public class SearchController {

    @Autowired
    private LEVService dvlaService;

    @PostMapping("/driver-search")
    public ResponseEntity<SearchResponse<?>> search(@RequestBody SearchRequest searchRequest) {
        log.log(Level.INFO, searchRequest.toString());
        return ResponseEntity.ok(new SearchResponse(dvlaService.searchByLastName(searchRequest.getCriteria().getLastName())));
    }
}



