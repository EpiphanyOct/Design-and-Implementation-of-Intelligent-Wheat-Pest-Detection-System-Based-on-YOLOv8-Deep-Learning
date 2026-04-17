package com.topwheat.pestdetect.service;

import com.topwheat.pestdetect.model.PestDetectionResponse;
import com.topwheat.pestdetect.repository.PestDetectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 害虫检测服务层单元测试
 * Feature: Pest Detection
 */
@ExtendWith(MockitoExtension.class)
public class PestDetectionServiceTest {

    @Mock
    private PestDetectionRepository pestDetectionRepository;

    @InjectMocks
    private PestDetectionService pestDetectionService;

    @BeforeEach
    void setUp() {
        when(pestDetectionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void testDetectPestByImage() {
        // Given
        byte[] imageBytes = new byte[]{ (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00 };

        // When
        PestDetectionResponse result = pestDetectionService.detectPestByImage(imageBytes);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getPestType());
        assertNotNull(result.getConfidence());
        assertTrue(result.getConfidence() >= 0.6 && result.getConfidence() <= 0.95);
        assertNotNull(result.getIdentifiedPests());
        assertFalse(result.getIdentifiedPests().isEmpty());
    }

    @Test
    void testBatchDetectPestsByImages() throws Exception {
        // Given
        MockMultipartFile file1 = new MockMultipartFile(
                "images", "test1.jpg", "image/jpeg", new byte[]{ 0x00 }
        );
        MockMultipartFile file2 = new MockMultipartFile(
                "images", "test2.jpg", "image/jpeg", new byte[]{ 0x01 }
        );
        List<MockMultipartFile> files = Arrays.asList(file1, file2);

        // When
        List<PestDetectionResponse> results = pestDetectionService.batchDetectPestsByImages(files);

        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.get(0).isSuccess());
        assertTrue(results.get(1).isSuccess());
    }

    @Test
    void testQueryPestInfoByName() {
        // When
        PestDetectionResponse result = pestDetectionService.queryPestInfoByName("红蜘蛛");

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("红蜘蛛", result.getPestType());
    }

    @Test
    void testQueryPestInfoByNameNotFound() {
        // When
        PestDetectionResponse result = pestDetectionService.queryPestInfoByName("不存在的害虫");

        // Then
        assertNull(result);
    }

    @Test
    void testGetSupportedPestTypes() {
        // When
        List<String> pestTypes = pestDetectionService.getSupportedPestTypes();

        // Then
        assertNotNull(pestTypes);
        assertTrue(pestTypes.size() > 0);
        assertTrue(pestTypes.contains("红蜘蛛"));
        assertTrue(pestTypes.contains("麦蚜"));
        assertTrue(pestTypes.contains("小麦螟虫"));
    }

    @Test
    void testDetectResultContainsAllFields() {
        // Given
        byte[] imageBytes = new byte[]{ 0x00, 0x01, 0x02, 0x03 };

        // When
        PestDetectionResponse result = pestDetectionService.detectPestByImage(imageBytes);

        // Then
        assertNotNull(result.getPestType());
        assertNotNull(result.getConfidence());
        assertNotNull(result.getDiagnosis());
        assertNotNull(result.getSuggestions());
        assertNotNull(result.getDetectTime());
        assertNotNull(result.getIdentifiedPests());
        
        PestDetectionResponse.IdentifiedPest pest = result.getIdentifiedPests().get(0);
        assertNotNull(pest.getPestId());
        assertNotNull(pest.getPestName());
        assertNotNull(pest.getConfidence());
        assertNotNull(pest.getDescription());
        assertNotNull(pest.getPreventionTips());
    }
}
