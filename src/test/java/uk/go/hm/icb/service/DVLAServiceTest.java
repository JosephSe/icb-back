package uk.go.hm.icb.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import uk.go.hm.icb.dto.DrivingLicenceRecord;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DVLAServiceTest {

    @Mock
    private ResourceLoader resourceLoader;

    @Mock
    private Resource resource;

    @InjectMocks
    private DVLAService dvlaService;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);

        String csvContent = "drivingLicenceNumber,lastName,gender\n" +
                "BOFGO313299CP25,Doe,Male\n" +
                "WOEOI472998XN72,Doe,Female\n" +
                "HXMKP655185WV29,Rodriguez,Male";

        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes());

        when(resourceLoader.getResource("classpath:driving_licence_records.csv")).thenReturn(resource);
        when(resource.getInputStream()).thenReturn(inputStream);

        dvlaService.init();
    }

    @Test
    void testLoadRecords() {
        // This test implicitly checks if records are loaded correctly in the setUp method
        List<DrivingLicenceRecord> records = dvlaService.searchByLastName("Doe");
        assertEquals(2, records.size());
    }

    @Test
    void testSearchByLastName_ExistingLastName() {
        List<DrivingLicenceRecord> results = dvlaService.searchByLastName("Doe");

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(record -> record.getLastName().equalsIgnoreCase("Doe")));
    }

    @Test
    void testSearchByLastName_NonExistingLastName() {
        List<DrivingLicenceRecord> results = dvlaService.searchByLastName("Johnson");

        assertTrue(results.isEmpty());
    }

    @Test
    void testSearchByLastName_CaseInsensitive() {
        List<DrivingLicenceRecord> results = dvlaService.searchByLastName("rodriguez");

        assertEquals(1, results.size());
        assertEquals("Rodriguez", results.get(0).getLastName());
    }

    @Test
    void testInit_ResourceLoadingError() throws IOException {
        // Simulate an IOException when loading the resource
        when(resource.getInputStream()).thenThrow(new IOException("Simulated IO error"));

        DVLAService newService = new DVLAService(resourceLoader);

        assertThrows(RuntimeException.class, newService::init);
    }
}