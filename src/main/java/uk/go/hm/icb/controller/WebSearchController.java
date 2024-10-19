package uk.go.hm.icb.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import uk.go.hm.icb.dto.Greeting;
import uk.go.hm.icb.dto.ICBRequest;
import uk.go.hm.icb.dto.ICBResponse;
import uk.go.hm.icb.dto.LEVRecord;
import uk.go.hm.icb.dto.SearchSource;
import uk.go.hm.icb.service.DVLAService;
import uk.go.hm.icb.service.LEVService;

import java.util.concurrent.CompletableFuture;


@Controller
public class WebSearchController {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DVLAService dvlaService;
    private final LEVService levService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public WebSearchController(DVLAService dvlaService, LEVService levService, SimpMessagingTemplate messagingTemplate) {
        this.dvlaService = dvlaService;
        this.levService = levService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/name-search")
    public void search(ICBRequest icbRequest) throws JsonProcessingException {
        // Search in DVLAService
        if (icbRequest.getSearchSources().contains(SearchSource.DVLA)) {
            CompletableFuture.runAsync(() -> {
                try {
                    ICBResponse response = dvlaService.search(icbRequest);
                    messagingTemplate.convertAndSend("/topic/results", new Greeting(objectMapper.writeValueAsString(response)));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        // Search in LEVService after a delay
//        CompletableFuture.runAsync(() -> {
//            try {
//                Thread.sleep(2000); // Delay for 2 seconds
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }
//            List<ICBResponse> levResults = levService.searchByLastName(lastName).stream()
//                    .map(ICBResponse::new)
//                    .toList();
//            messagingTemplate.convertAndSend("/topic/results", new Greeting(levResults.toString()));
//        });
    }
}