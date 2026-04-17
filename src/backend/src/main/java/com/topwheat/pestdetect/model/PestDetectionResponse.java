package com.topwheat.pestdetect.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 害虫检测响应类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PestDetectionResponse {

    private boolean success;
    private String message;
    private Object data;

    // 检测详情
    private String pestType;
    private Double confidence;
    private String diagnosis;
    private String suggestions;
    private String imageUrl;
    private String detectTime;

    // 批量检测结果
    private List<IdentifiedPest> identifiedPests;

    public PestDetectionResponse(String message) {
        this.message = message;
    }

    public PestDetectionResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public PestDetectionResponse(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    /**
     * 识别的害虫信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IdentifiedPest {
        private String pestId;
        private String pestName;
        private Double confidence;
        private String description;
        private List<String> preventionTips;
    }
}
