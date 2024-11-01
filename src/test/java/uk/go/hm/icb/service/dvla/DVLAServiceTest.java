package uk.go.hm.icb.service.dvla;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.go.hm.icb.dto.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DVLAServiceTest {

    @Mock
    private DVLADataLoader mockDataLoader;

    @InjectMocks
    private DVLAService dvlaService;
    private TestDVLADataLoader dataLoader;

    @BeforeEach
    void setUp() {
        dataLoader = new TestDVLADataLoader();
        dvlaService = new DVLAService(mockDataLoader);
    }

    @Test
    void search_WithDrivingLicense_SingleMatch() {
        // Arrange
        when(mockDataLoader.getRecords()).thenReturn(dataLoader.getRecords());
        SearchIdentifiers driverLicense = SearchIdentifiers.builder()
                .idType(SearchIDType.DRIVER_LICENSE)
                .idValue("D12345678")
                .build();

        ICBRequest request = ICBRequest.builder()
                .searchBioDetails(SearchBioDetails.builder()
                        .build())
                .searchIDTypes(List.of(driverLicense))
                .build();

        // Act
        ICBResponse response = dvlaService.search(request);

        // Assert
        assertEquals("One match found", response.getMatchStatus());
        assertTrue(response.getSearchComplete());
        assertEquals(SearchSource.DVLA, response.getSearchSource());
        assertNotNull(response.getMatch());

        //First Name
        assertEquals("No", response.getMatch().getMatches().get(0).getSecond());
        //Last Name
        assertEquals("NO", response.getMatch().getMatches().get(1).getSecond());
        //Middle Name
        assertEquals("-", response.getMatch().getMatches().get(2).getSecond());
        //DOB
        assertEquals("No", response.getMatch().getMatches().get(3).getSecond());
        //Address
        assertEquals("-", response.getMatch().getMatches().get(4).getSecond());
        //LEV matched
        assertEquals("-", response.getMatch().getMatches().get(4).getSecond());
        //DL matched
        assertEquals("YES", response.getMatch().getMatches().get(6).getSecond());
    }

    @Test
    void search_WithLastNameOnly_SingleMatche() {
        // Arrange
        when(mockDataLoader.getRecords()).thenReturn(dataLoader.getRecords());
        ICBRequest request = ICBRequest.builder()
                .searchBioDetails(SearchBioDetails.builder()
                        .lastName("Johnson")
                        .build())
                .build();

        // Act
        ICBResponse response = dvlaService.search(request);

        // Assert
        assertEquals("One match found", response.getMatchStatus());
        assertTrue(response.getSearchComplete());
        assertEquals(SearchSource.DVLA, response.getSearchSource());
        assertNotNull(response.getMatch());

        //First Name
        assertEquals("No", response.getMatch().getMatches().get(0).getSecond());
        //Last Name
        assertEquals("YES", response.getMatch().getMatches().get(1).getSecond());
        //Middle Name
        assertEquals("-", response.getMatch().getMatches().get(2).getSecond());
        //DOB
        assertEquals("No", response.getMatch().getMatches().get(3).getSecond());
        //Address
        assertEquals("-", response.getMatch().getMatches().get(4).getSecond());
        //LEV matched
        assertEquals("-", response.getMatch().getMatches().get(4).getSecond());
        //DL matched
        assertEquals("NO", response.getMatch().getMatches().get(6).getSecond());
    }

    @Test
    void search_WithLastNameOnly_MultipleMatches() {
        // Arrange
        when(mockDataLoader.getRecords()).thenReturn(dataLoader.getRecords());
        ICBRequest request = ICBRequest.builder()
                .searchBioDetails(SearchBioDetails.builder()
                        .lastName("Smith")
                        .build())
                .build();

        // Act
        ICBResponse response = dvlaService.search(request);

        // Assert
        assertEquals("Multiple matches found", response.getMatchStatus());
        assertTrue(response.getSearchComplete());
        assertNotNull(response.getMultiMatches());
        assertEquals(2, response.getMultiMatches().size());

        // Verify both matches are for Smith
        response.getMultiMatches().forEach(match ->
                assertEquals("Smith", match.getLastName())
        );
    }

    @Test
    void search_NoMatch() {
        // Arrange
        when(mockDataLoader.getRecords()).thenReturn(dataLoader.getRecords());
        ICBRequest request = ICBRequest.builder()
                .searchBioDetails(SearchBioDetails.builder()
                        .lastName("NonExistent")
                        .build())
                .build();

        // Act
        ICBResponse response = dvlaService.search(request);

        // Assert
        assertEquals("No match found", response.getMatchStatus());
        assertTrue(response.getSearchComplete());
    }

    @Test
    void search_WithInvalidDrivingLicense_NoMatch() {
        // Arrange
        when(mockDataLoader.getRecords()).thenReturn(dataLoader.getRecords());
        SearchIdentifiers driverLicense = SearchIdentifiers.builder()
                .idType(SearchIDType.DRIVER_LICENSE)
                .idValue("INVALID123")
                .build();

        ICBRequest request = ICBRequest.builder()
                .searchBioDetails(SearchBioDetails.builder()
                        .lastName("Doe")
                        .build())
                .searchIDTypes(List.of(driverLicense))
                .build();

        // Act
        ICBResponse response = dvlaService.search(request);

        // Assert
        assertEquals("No match found", response.getMatchStatus());
    }

    @Test
    void searchByLastName_Success() {
        // Arrange
        when(mockDataLoader.getRecords()).thenReturn(dataLoader.getRecords());

        // Act
        List<DrivingLicenceRecord> results = dvlaService.searchByLastName("Doe");

        // Assert
        assertEquals(2, results.size());
        results.forEach(record -> assertEquals("Doe", record.getLastName()));
    }

    @Test
    void searchByLastName_CaseInsensitive() {
        // Arrange
        when(mockDataLoader.getRecords()).thenReturn(dataLoader.getRecords());

        // Act
        List<DrivingLicenceRecord> results = dvlaService.searchByLastName("doe");

        // Assert
        assertEquals(2, results.size());
        results.forEach(record -> assertEquals("Doe", record.getLastName()));
    }

    @Test
    void searchByLastName_NoMatch() {
        // Act
        List<DrivingLicenceRecord> results = dvlaService.searchByLastName("NonExistent");

        // Assert
        assertTrue(results.isEmpty());
    }

    @Test
    void search_WithEmptyDrivingLicense_ShouldIgnore() {
        // Arrange
        when(mockDataLoader.getRecords()).thenReturn(dataLoader.getRecords());
        SearchIdentifiers driverLicense = SearchIdentifiers.builder()
                .idType(SearchIDType.DRIVER_LICENSE)
                .idValue("")
                .build();

        ICBRequest request = ICBRequest.builder()
                .searchBioDetails(SearchBioDetails.builder()
                        .lastName("Doe")
                        .build())
                .searchIDTypes(List.of(driverLicense))
                .build();

        // Act
        ICBResponse response = dvlaService.search(request);

        // Assert
        assertEquals("Multiple matches found", response.getMatchStatus());
        assertEquals(2, response.getMultiMatches().size());
    }
}