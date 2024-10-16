package uk.go.hm.icb.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import uk.go.hm.icb.dto.DrivingLicenceRecord;
import uk.go.hm.icb.dto.LEVRecord;
import uk.go.hm.icb.service.DVLAService;
import uk.go.hm.icb.service.LEVService;

import java.util.List;
import java.util.concurrent.CompletableFuture;


@Controller
public class WebSearchController {

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
    public void search(String lastName) {
        // Search in DVLAService
        List<DrivingLicenceRecord> dvlaResults = dvlaService.searchByLastName(lastName);
        messagingTemplate.convertAndSend("/topic/results", dvlaResults);

        // Search in LEVService after a delay
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(2000); // Delay for 2 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            List<LEVRecord> levResults = levService.searchByLastName(lastName);
            messagingTemplate.convertAndSend("/topic/results", levResults);
        });
    }
}