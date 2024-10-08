package uk.go.hm.icb.controller;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.go.hm.icb.dto.Criteria;
import uk.go.hm.icb.dto.SearchRequest;
import uk.go.hm.icb.dto.SearchResponse;
import uk.go.hm.icb.service.DVLAService;

import java.util.logging.Level;

import static java.lang.System.Logger.Level.DEBUG;

@Log
@RestController
@RequestMapping("/api")
public class SearchController {

    @Autowired
    private DVLAService dvlaService;

    @PostMapping("/driver-search")
    public ResponseEntity<SearchResponse<?>> search(@RequestBody SearchRequest searchRequest) {
        log.log(Level.INFO, searchRequest.toString());
        return ResponseEntity.ok(new SearchResponse(dvlaService.searchByLastName(searchRequest.getCriteria().getLastName())));
    }
}



