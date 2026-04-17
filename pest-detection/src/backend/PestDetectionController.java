package com.topwheat.pestdetect.controller;

import com.topwheat.pestdetect.model.PestDetectionRecord;
import com.topwheat.pestdetect.model.PestDetectionResponse;
import com.topwheat.pestdetect.service.PestDetectionService;
import com.topwheat.pestdetect.util.ImageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 害虫检测控制器
 * Feature: Pest Detection
 */
@RestController
@RequestMapping("/api/wheat-pest-detection")
public class PestDetectionController {

    @Autowired
    private PestDetectionService pestDetectionService;

    /**
     * 单张图片害虫检测
     * POST /api/wheat-pest-detection/detect
     */
    @PostMapping(value = "/detect", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PestDetectionResponse> detectPestByImage(
            @RequestPart("image") MultipartFile image) {
        
        // 1. 参数校验
        if (image == null || image.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new PestDetectionResponse(false, "上传图片不能为空"));
        }

        // 2. 格式校验
        String contentType = image.getContentType();
        if (!isValidImageType(contentType)) {
            return ResponseEntity.badRequest()
                    .body(new PestDetectionResponse(false, "只支持JPG/PNG/BMP格式图片"));
        }

        try {
            // 3. 图片内容校验
            byte[] imageBytes = image.getBytes();
            if (!ImageUtils.isImageValid(imageBytes)) {
                return ResponseEntity.badRequest()
                        .body(new PestDetectionResponse(false, "图片内容不合法"));
            }

            // 4. 调用检测服务
            PestDetectionResponse result = pestDetectionService.detectPestByImage(imageBytes);
            
            if (result.getIdentifiedPests() == null || result.getIdentifiedPests().isEmpty()) {
                return ResponseEntity.ok(new PestDetectionResponse(true, "未检测到害虫"));
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new PestDetectionResponse(false, "检测失败: " + e.getMessage()));
        }
    }

    /**
     * 批量害虫检测
     * POST /api/wheat-pest-detection/batch-detect
     */
    @PostMapping(value = "/batch-detect", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<PestDetectionResponse>> batchDetectPestsByImages(
            @RequestPart("images") List<MultipartFile> images) {

        // 1. 参数校验
        if (images == null || images.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (images.size() > 20) {
            return ResponseEntity.badRequest().build();
        }

        // 2. 校验每张图片
        for (MultipartFile image : images) {
            if (!isValidImage(image)) {
                return ResponseEntity.badRequest().build();
            }
        }

        try {
            // 3. 批量检测
            List<PestDetectionResponse> results = pestDetectionService.batchDetectPestsByImages(images);
            return ResponseEntity.ok(results);

        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 查询检测历史
     * GET /api/wheat-pest-detection/history
     */
    @GetMapping("/history")
    public ResponseEntity<List<PestDetectionResponse>> queryPestDetectionHistory(
            @RequestParam(value = "startTime", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(value = "endTime", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {

        // 时间范围校验
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            return ResponseEntity.badRequest().build();
        }

        try {
            List<PestDetectionResponse> history = pestDetectionService
                    .queryDetectionHistory(startTime, endTime, page, pageSize);
            return ResponseEntity.ok(history);

        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 查询害虫信息
     * GET /api/wheat-pest-detection/pest-info
     */
    @GetMapping("/pest-info")
    public ResponseEntity<PestDetectionResponse> queryPestInfoByName(
            @RequestParam("pestName") String pestName) {

        if (!StringUtils.hasText(pestName) || pestName.trim().length() > 50) {
            return ResponseEntity.badRequest().build();
        }

        try {
            PestDetectionResponse pestInfo = pestDetectionService
                    .queryPestInfoByName(pestName.trim());
            
            if (pestInfo == null) {
                return ResponseEntity.ok(new PestDetectionResponse(true, "未找到对应害虫信息"));
            }

            return ResponseEntity.ok(pestInfo);

        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // ============ 私有方法 ============

    private boolean isValidImageType(String contentType) {
        if (contentType == null) return false;
        return contentType.equalsIgnoreCase(MediaType.IMAGE_JPEG_VALUE)
                || contentType.equalsIgnoreCase(MediaType.IMAGE_PNG_VALUE)
                || contentType.equalsIgnoreCase("image/bmp");
    }

    private boolean isValidImage(MultipartFile image) {
        if (image == null || image.isEmpty()) return false;
        if (!isValidImageType(image.getContentType())) return false;
        try {
            return ImageUtils.isImageValid(image.getBytes());
        } catch (Exception e) {
            return false;
        }
    }
}
