package uk.go.hm.icb.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import uk.go.hm.icb.dto.ICBRequest;
import uk.go.hm.icb.dto.ICBResponse;
import uk.go.hm.icb.dto.SearchSource;
import uk.go.hm.icb.service.SearchStrategy;
import uk.go.hm.icb.service.dvla.DVLAService;
import uk.go.hm.icb.service.lev.LEVService;

import java.util.Map;
import java.util.concurrent.CompletableFuture;


@Controller
@AllArgsConstructor
public class WebSearchController {

    private final DVLAService dvlaService;
    private final LEVService levService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ObjectMapper objectMapper;
    final String resultsTopic = "/topic/results";
    private Map<SearchSource, SearchStrategy> searchStrategies;

    @Autowired
    public WebSearchController(DVLAService dvlaService, LEVService levService, SimpMessagingTemplate simpMessagingTemplate, ObjectMapper objectMapper) {
        this.dvlaService = dvlaService;
        this.levService = levService;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.objectMapper = objectMapper;
        searchStrategies = Map.of(
                SearchSource.DVLA, dvlaService,
                SearchSource.LEV, levService,
                SearchSource.IPCS, dvlaService
        );
    }

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
     */
    @MessageMapping("/search")
    public void search(ICBRequest icbRequest, StompHeaderAccessor headerAccessor) {
        final String sessionId = headerAccessor.getSessionId();

        icbRequest.getSearchSources().stream()
                .filter(searchStrategies::containsKey)
                .forEach(source -> executeSearch(searchStrategies.get(source), icbRequest, sessionId));
    }

    private void executeSearch(SearchStrategy strategy, ICBRequest request, String sessionId) {
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(strategy.getDelay());
                ICBResponse response = strategy.search(request);
                sendResponse(response, sessionId, resultsTopic);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void sendResponse(ICBResponse response, String sessionId, String topic) throws JsonProcessingException {
        simpMessagingTemplate.convertAndSendToUser(sessionId, topic, objectMapper.writeValueAsString(response), createHeaders(sessionId));
    }
}