package uk.go.hm.icb.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.go.hm.icb.dto.Criteria;
import uk.go.hm.icb.dto.DrivingLicenceRecord;
import uk.go.hm.icb.dto.SearchRequest;
import uk.go.hm.icb.dto.SearchResponse;
import uk.go.hm.icb.service.LEVService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SearchControllerTest {

    @Mock
    private LEVService dvlaService;

    @InjectMocks
    private SearchController searchController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSearch_SuccessfulSearch() {
        // Arrange
        SearchRequest searchRequest = new SearchRequest();
        Criteria criteria = new Criteria();
        criteria.setLastName("Smith");
        searchRequest.setCriteria(criteria);

        DrivingLicenceRecord driverInfo = new DrivingLicenceRecord("John", "Smith", "123456789");
        when(dvlaService.searchByLastName("Smith")).thenReturn(List.of(driverInfo));

        // Act
        ResponseEntity<SearchResponse<?>> response = searchController.search(searchRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        // Use a type-safe cast with a wildcard
        List<DrivingLicenceRecord> results = (List<DrivingLicenceRecord>) response.getBody().getResults();
        assertEquals(driverInfo, results.get(0));

        // Verify that the service method was called
        verify(dvlaService, times(1)).searchByLastName("Smith");
    }

    @Test
    void testSearch_NoResultFound() {
        // Arrange
        SearchRequest searchRequest = new SearchRequest();
        Criteria criteria = new Criteria();
        criteria.setLastName("NonExistent");
        searchRequest.setCriteria(criteria);

        when(dvlaService.searchByLastName("NonExistent")).thenReturn(null);

        // Act
        ResponseEntity<SearchResponse<?>> response = searchController.search(searchRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().getResults());

        // Verify that the service method was called
        verify(dvlaService, times(1)).searchByLastName("NonExistent");
    }

    @Test
    void testSearch_NullSearchRequest() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> searchController.search(null));
    }

    @Test
    void testSearch_NullCriteria() {
        // Arrange
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setCriteria(null);

        // Act & Assert
        assertThrows(NullPointerException.class, () -> searchController.search(searchRequest));
    }

}