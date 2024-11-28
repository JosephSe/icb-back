package uk.go.hm.icb.service.ipcs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.go.hm.icb.dto.*;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IPCSServiceTest {

    @Mock
    private IPCSDataLoader ipcsDataLoader;

    private IPCSService ipcsService;

    private static final long DELAY = 1000L;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ipcsService = new IPCSService(ipcsDataLoader, DELAY);
    }

    @Test
    void testGetDelay() {
        assertEquals(DELAY, ipcsService.getDelay());
    }

    @Test
    void testSearch_NoMatch() {
        // Given
        ICBRequest request = createTestRequest("John", "Doe", "PP123456");
        when(ipcsDataLoader.getRecords()).thenReturn(Collections.emptyList());

        // When
        ICBResponse response = ipcsService.search(request);

        // Then
        assertNotNull(response);
        assertEquals("No match found", response.getMatchStatus());
        assertTrue(response.getSearchComplete());
        assertEquals(SearchSource.IPCS, response.getSearchSource());
        assertNull(response.getMatch());
        assertNull(response.getMultiMatches());
    }

    @Test
    void testSearch_SingleMatch() {
        // Given
        String firstName = "John";
        String lastName = "Doe";
        String passportNumber = "PP123456";
        
        ICBRequest request = createTestRequest(firstName, lastName, passportNumber);
        
        IPCSRecord record = new IPCSRecord(firstName, lastName, passportNumber);

        
        when(ipcsDataLoader.getRecords()).thenReturn(Collections.singletonList(record));

        // When
        ICBResponse response = ipcsService.search(request);

        // Then
        assertNotNull(response);
        assertEquals("One match found", response.getMatchStatus());
        assertTrue(response.getSearchComplete());
        assertEquals(SearchSource.IPCS, response.getSearchSource());
        assertNotNull(response.getMatch());
        assertEquals("YES", response.getMatch().getMatches().get(0).getSecond()); // firstName match
        assertEquals("YES", response.getMatch().getMatches().get(1).getSecond()); // lastName match
        assertEquals("-", response.getMatch().getMatches().get(2).getSecond()); // middle name match
        assertEquals("-", response.getMatch().getMatches().get(3).getSecond()); // dob match
        assertEquals("-", response.getMatch().getMatches().get(4).getSecond()); // address match
        assertEquals("YES", response.getMatch().getMatches().get(5).getSecond()); // passport number match
    }

    @Test
    void testSearch_SingleMatche_MultiData() {
        // Given
        String firstName = "John";
        String lastName = "Doe";
        String passportNumber = "PP123456";
        
        ICBRequest request = createTestRequest(firstName, lastName, passportNumber);

        IPCSRecord record1 = new IPCSRecord(firstName, lastName, passportNumber);
        
        IPCSRecord record2 = new IPCSRecord(firstName,lastName,"PP789012");
        
        when(ipcsDataLoader.getRecords()).thenReturn(Arrays.asList(record1, record2));

        // When
        ICBResponse response = ipcsService.search(request);

        // Then
        assertNotNull(response);
        assertEquals("One match found", response.getMatchStatus());
        assertTrue(response.getSearchComplete());
        assertEquals(SearchSource.IPCS, response.getSearchSource());
        assertNotNull(response.getMatch());
        assertNull(response.getMultiMatches());
    }

    private ICBRequest createTestRequest(String firstName, String lastName, String passportNumber) {
        SearchBioDetails bioDetails = SearchBioDetails.builder()
                .firstName(firstName)
                .lastName(lastName)
                .build();

        SearchIdentifiers identifier = SearchIdentifiers.builder()
                .idType(SearchIDType.IPCS_PPT_NUM)
                .idValue(passportNumber)
                .build();

        return ICBRequest.builder()
                .searchBioDetails(bioDetails)
                .searchIDTypes(Collections.singletonList(identifier))
                .build();
    }
}
