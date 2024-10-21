package uk.go.hm.icb.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import uk.go.hm.icb.dto.Greeting;
import uk.go.hm.icb.dto.ICBRequest;
import uk.go.hm.icb.dto.ICBResponse;
import uk.go.hm.icb.dto.SearchSource;
import uk.go.hm.icb.service.DVLAService;
import uk.go.hm.icb.service.LEVService;

import java.util.concurrent.CompletableFuture;


@Controller
@AllArgsConstructor
public class WebSearchController {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DVLAService dvlaService;
    private final LEVService levService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    private MessageHeaders createHeaders(String sessionId) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor
                .create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
        return headerAccessor.getMessageHeaders();
    }

    /**
     * test with the below 2 json in the name field
     * {"searchSources":["DVLA"], "searchBioDetails":{"firstName":"jane","lastName":"smith"}}
     * {"searchSources":["DVLA"], "searchIDTypes":[{"searchSource":"DVLA","searchIDType":"DRIVER_LICENSE","value":"D87654322"}], "searchBioDetails":{"firstName":"jane","lastName":"smith"}}
     * */
    @MessageMapping("/search")
    public void search(ICBRequest icbRequest, StompHeaderAccessor headerAccessor) throws JsonProcessingException {
        final String sessionId = headerAccessor.getSessionId();
        // Search in DVLAService
        if (icbRequest.getSearchSources().contains(SearchSource.DVLA)) {
        CompletableFuture.runAsync(() -> {
                try {
                    ICBResponse response = dvlaService.search(icbRequest);
                    simpMessagingTemplate.convertAndSendToUser(sessionId, "/topic/results", new Greeting(objectMapper.writeValueAsString(response)), createHeaders(sessionId));
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