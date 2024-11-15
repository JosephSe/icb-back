package uk.go.hm.icb.service.lev;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.go.hm.icb.dto.ICBRequest;
import uk.go.hm.icb.dto.ICBResponse;
import uk.go.hm.icb.dto.LEVRecord;
import uk.go.hm.icb.dto.SearchBioDetails;
import uk.go.hm.icb.dto.SearchIDType;
import uk.go.hm.icb.dto.SearchIdentifiers;
import uk.go.hm.icb.dto.SearchSource;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class LEVServiceTest {

    @Mock
    private LEVDataLoader dataLoaderService;

    private LEVService levService;
    private static final long DELAY = 100L;

    @BeforeEach
    void setUp() {
        levService = new LEVService(dataLoaderService, DELAY);
    }

    @Test
    void searchWithBirthCertificate_SingleMatch() {
        // Arrange
        LEVRecord record = LEVRecord.builder()
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .birthCertificate("BC123")
                .flag("RED")
                .build();

        SearchIdentifiers birthCertIdentifier = SearchIdentifiers.builder()
                .idType(SearchIDType.BIRTH_CERTIFICATE)
                .idValue("BC123")
                .build();

        SearchBioDetails bioDetails = SearchBioDetails.builder()
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .build();

        ICBRequest request = ICBRequest.builder()
                .searchIDTypes(Collections.singletonList(birthCertIdentifier))
                .searchBioDetails(bioDetails)
                .build();

        when(dataLoaderService.getRecords()).thenReturn(Collections.singletonList(record));

        // Act
        ICBResponse response = levService.search(request);

        // Assert
        assertNotNull(response);
        assertEquals(SearchSource.LEV, response.getSearchSource());
        assertEquals("One match found", response.getMatchStatus());
        assertTrue(response.getSearchComplete());
        assertEquals("YES", response.getMatch().getMatches().get(0).getSecond()); // firstName match
        assertEquals("YES", response.getMatch().getMatches().get(1).getSecond()); // lastName match
        assertEquals("-", response.getMatch().getMatches().get(2).getSecond()); // middle name match
        assertEquals("YES", response.getMatch().getMatches().get(3).getSecond()); // dob match
        assertEquals("-", response.getMatch().getMatches().get(4).getSecond()); // address match
        assertEquals("YES", response.getMatch().getMatches().get(5).getSecond()); // lev match
        assertEquals("-", response.getMatch().getMatches().get(6).getSecond()); // dvla match
        assertEquals("-", response.getMatch().getMatches().get(7).getSecond()); // passport number match
        assertEquals("RED", response.getMatch().getMatches().get(8).getSecond()); // flag

    }

    @Test
    void searchWithBioDetails_MultipleMatches() {
        // Arrange
        LEVRecord record1 = LEVRecord.builder()
                .firstName("John")
                .lastName("Smith")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .birthCertificate("BC123")
                .build();

        LEVRecord record2 = LEVRecord.builder()
                .firstName("John")
                .lastName("Smith")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .birthCertificate("BC456")
                .build();

        SearchBioDetails bioDetails = SearchBioDetails.builder()
                .firstName("John")
                .lastName("Smith")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .build();

        ICBRequest request = ICBRequest.builder()
                .searchBioDetails(bioDetails)
                .build();

        when(dataLoaderService.getRecords()).thenReturn(Arrays.asList(record1, record2));

        // Act
        ICBResponse response = levService.search(request);

        // Assert
        assertNotNull(response);
        assertEquals(SearchSource.LEV, response.getSearchSource());
        assertEquals("Multiple matches found", response.getMatchStatus());
        assertTrue(response.getSearchComplete());
        assertEquals(2, response.getMultiMatches().size());
    }

    @Test
    void searchWithNoMatches() {
        // Arrange
        LEVRecord record = LEVRecord.builder()
                .firstName("Jane")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .birthCertificate("BC123")
                .build();

        SearchBioDetails bioDetails = SearchBioDetails.builder()
                .firstName("John")
                .lastName("Smith")
                .dateOfBirth(LocalDate.of(1991, 1, 1))
                .build();

        ICBRequest request = ICBRequest.builder()
                .searchBioDetails(bioDetails)
                .build();

        when(dataLoaderService.getRecords()).thenReturn(Collections.singletonList(record));

        // Act
        ICBResponse response = levService.search(request);

        // Assert
        assertNotNull(response);
        assertEquals(SearchSource.LEV, response.getSearchSource());
        assertEquals("No match found", response.getMatchStatus());
        assertTrue(response.getSearchComplete());
    }

}