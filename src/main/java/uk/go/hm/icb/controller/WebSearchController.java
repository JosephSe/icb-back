package uk.go.hm.icb.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import uk.go.hm.icb.dto.Greeting;
import uk.go.hm.icb.dto.ICBMatch;
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
                    Thread.sleep(1000);
                    ICBResponse response = dvlaService.search(icbRequest);
                    simpMessagingTemplate.convertAndSendToUser(sessionId, "/topic/results", objectMapper.writeValueAsString(response), createHeaders(sessionId));
                } catch (JsonProcessingException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
        });
        }
        if (icbRequest.getSearchSources().contains(SearchSource.LEV)) {
            // Search in LEVService after a delay
            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(10000); // Delay for 10 seconds
                    ICBResponse response = dvlaService.search(icbRequest);
                    ICBResponse.ICBResponseBuilder responseBuilder = response.toBuilder();
                    ICBMatch match = ICBMatch.builder()
                            .matches("YES", "YES", "NO", "YES", "YES", "NO", "NO")
                            .verification("Name Match - 100%").verification("Birth Cert Match - 100%")
                            .build();
                    response = responseBuilder.searchSource(SearchSource.LEV).match(match).build();
                    simpMessagingTemplate.convertAndSendToUser(sessionId, "/topic/results", objectMapper.writeValueAsString(response), createHeaders(sessionId));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        if (icbRequest.getSearchSources().contains(SearchSource.IPCS)) {
            // Search in LEVService after a delay
            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(5000); // Delay for 10 seconds
                    ICBResponse response = dvlaService.search(icbRequest);
                    ICBResponse.ICBResponseBuilder responseBuilder = response.toBuilder();
                    ICBMatch match = ICBMatch.builder()
                            .matches("YES", "YES", "NO", "YES", "YES", "-", "-")
                            .verification("Name Match - 100%").verification("Birth Cert Match - 100%")
                            .build();
                    response = responseBuilder.searchSource(SearchSource.IPCS).match(match).build();
                    simpMessagingTemplate.convertAndSendToUser(sessionId, "/topic/results", objectMapper.writeValueAsString(response), createHeaders(sessionId));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}