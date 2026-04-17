package com.topwheat.pestdetect.controller;

import com.topwheat.pestdetect.model.PestDetectionResponse;
import com.topwheat.pestdetect.service.PestDetectionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 害虫检测控制器单元测试
 * Feature: Pest Detection
 */
@WebMvcTest(PestDetectionController.class)
public class PestDetectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PestDetectionService pestDetectionService;

    @Test
    void testDetectPestByImageSuccess() throws Exception {
        // Given
        PestDetectionResponse mockResponse = new PestDetectionResponse();
        mockResponse.setSuccess(true);
        mockResponse.setPestType("红蜘蛛");
        mockResponse.setConfidence(0.92);
        mockResponse.setDiagnosis("寄生于小麦叶片，取食汁液导致叶片枯黄");
        
        when(pestDetectionService.detectPestByImage(any())).thenReturn(mockResponse);

        MockMultipartFile file = new MockMultipartFile(
                "image",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[]{ (byte) 0xFF, (byte) 0xD8, (byte) 0xFF }
        );

        // When & Then
        mockMvc.perform(multipart("/api/wheat-pest-detection/detect")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.pestType").value("红蜘蛛"))
                .andExpect(jsonPath("$.confidence").value(0.92));
    }

    @Test
    void testDetectPestByImageEmptyFile() throws Exception {
        // Given
        MockMultipartFile emptyFile = new MockMultipartFile(
                "image",
                "",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[]{}
        );

        // When & Then
        mockMvc.perform(multipart("/api/wheat-pest-detection/detect")
                        .file(emptyFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("上传图片不能为空"));
    }

    @Test
    void testDetectPestByImageInvalidFormat() throws Exception {
        // Given
        MockMultipartFile invalidFile = new MockMultipartFile(
                "image",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "test content".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/wheat-pest-detection/detect")
                        .file(invalidFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("只支持JPG/PNG/BMP格式图片"));
    }

    @Test
    void testBatchDetectSuccess() throws Exception {
        // Given
        List<PestDetectionResponse> mockResults = Arrays.asList(
                createMockResponse("红蜘蛛", 0.92),
                createMockResponse("麦蚜", 0.85)
        );
        when(pestDetectionService.batchDetectPestsByImage(any())).thenReturn(mockResults);

        MockMultipartFile file1 = new MockMultipartFile(
                "images",
                "test1.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[]{ (byte) 0xFF, (byte) 0xD8, (byte) 0xFF }
        );
        MockMultipartFile file2 = new MockMultipartFile(
                "images",
                "test2.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[]{ (byte) 0xFF, (byte) 0xD8, (byte) 0xFF }
        );

        // When & Then
        mockMvc.perform(multipart("/api/wheat-pest-detection/batch-detect")
                        .file(file1)
                        .file(file2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testQueryHistorySuccess() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/wheat-pest-detection/history")
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void testQueryHistoryInvalidTimeRange() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/wheat-pest-detection/history")
                        .param("startTime", "2024-01-10 00:00:00")
                        .param("endTime", "2024-01-01 00:00:00"))
                .andExpect(status().isBadRequest());
    }

    private PestDetectionResponse createMockResponse(String pestType, double confidence) {
        PestDetectionResponse response = new PestDetectionResponse();
        response.setSuccess(true);
        response.setPestType(pestType);
        response.setConfidence(confidence);
        return response;
    }
}
