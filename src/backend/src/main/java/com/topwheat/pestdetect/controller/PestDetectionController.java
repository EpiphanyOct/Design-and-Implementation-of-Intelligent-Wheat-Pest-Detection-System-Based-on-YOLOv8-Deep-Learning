package com.topwheat.pestdetect.controller;

import com.topwheat.pestdetect.model.PestDetectionRequest;
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
 * 处理害虫图片上传、识别、历史查询等请求
 */
@RestController
@RequestMapping("/api/wheat-pest-detection")
public class PestDetectionController {

    @Autowired
    private PestDetectionService pestDetectionService;

    /**
     * 上传图片进行害虫种类识别，返回诊断结果和防治建议
     * @param image 图片文件
     * @return 识别结果包含害虫种类，置信度，防治建议等
     */
    @PostMapping(value = "/detect", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PestDetectionResponse> detectPestByImage(@RequestPart("image") MultipartFile image) {
        // 1. 参数校验：校验文件不为空且格式合法
        if (image == null || image.isEmpty()) {
            return ResponseEntity.badRequest().body(new PestDetectionResponse("上传图片不能为空"));
        }
        String contentType = image.getContentType();
        if (contentType == null || !(contentType.equalsIgnoreCase(MediaType.IMAGE_JPEG_VALUE)
                || contentType.equalsIgnoreCase(MediaType.IMAGE_PNG_VALUE)
                || contentType.equalsIgnoreCase("image/bmp"))) {
            return ResponseEntity.badRequest().body(new PestDetectionResponse("上传文件格式不支持，请上传jpg、png或bmp格式图片"));
        }
        try {
            // 2. 读取图片字节数组进行数据完整性校验
            byte[] imageBytes = image.getBytes();
            if (imageBytes.length == 0) {
                return ResponseEntity.badRequest().body(new PestDetectionResponse("图片内容为空"));
            }
            // 3. 进一步校验图片内容是否合法（基本检测）
            if (!ImageUtils.isImageValid(imageBytes)) {
                return ResponseEntity.badRequest().body(new PestDetectionResponse("图片内容不合法"));
            }
            // 4. 调用业务层，智能分析图片，识别害虫种类，置信度，诊断结果及防治措施
            PestDetectionResponse detectionResponse = pestDetectionService.detectPestByImage(imageBytes);
            if (detectionResponse == null || detectionResponse.getIdentifiedPests() == null
                    || detectionResponse.getIdentifiedPests().isEmpty()) {
                return ResponseEntity.ok(new PestDetectionResponse("未检测到害虫"));
            }
            // 5. 返回结果
            return ResponseEntity.ok(detectionResponse);
        } catch (Exception e) {
            // 捕获业务异常和运行时异常，返回系统错误提示
            return ResponseEntity.status(500).body(new PestDetectionResponse("系统异常：" + e.getMessage()));
        }
    }

    /**
     * 查询害虫检测历史记录
     * @param startTime 查询起始时间，格式yyyy-MM-dd HH:mm:ss，可选
     * @param endTime 查询结束时间，格式yyyy-MM-dd HH:mm:ss，可选
     * @return 查询时间筛选的虫害诊断记录列表
     */
    @GetMapping("/history")
    public ResponseEntity<List<PestDetectionResponse>> queryPestDetectionHistory(
            @RequestParam(value = "startTime", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(value = "endTime", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        try {
            // 1. 参数业务校验
            if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
                return ResponseEntity.badRequest().build();
            }
            List<PestDetectionResponse> historyList = pestDetectionService.queryDetectionHistory(startTime, endTime);
            // 3. 如果无数据，返回空列表
            if (historyList == null || historyList.isEmpty()) {
                return ResponseEntity.ok().body(List.of());
            }
            // 4. 返回历史数据列表
            return ResponseEntity.ok(historyList);
        } catch (Exception e) {
            // 捕获异常，返回系统错误
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 批量害虫检测
     * 支持批量处理功能
     * @param images 图片列表，必须非空且数量不可超过20张
     * @return 批量检测结果列表（对应每张图片）
     */
    @PostMapping(value = "/batch-detect", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<PestDetectionResponse>> batchDetectPestsByImages(@RequestPart("images") List<MultipartFile> images) {
        // 1. 参数校验：不能为空，不能超过20张
        if (images == null || images.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (images.size() > 20) {
            return ResponseEntity.badRequest().build();
        }
        try {
            // 2. 循环校验单张图片格式及内容合法性
            for (MultipartFile image : images) {
                if (image == null || image.isEmpty()) {
                    return ResponseEntity.badRequest().build();
                }
                String contentType = image.getContentType();
                if (contentType == null || !(contentType.equalsIgnoreCase(MediaType.IMAGE_JPEG_VALUE)
                        || contentType.equalsIgnoreCase(MediaType.IMAGE_PNG_VALUE)
                        || contentType.equalsIgnoreCase("image/bmp"))) {
                    return ResponseEntity.badRequest().build();
                }
                if (!ImageUtils.isImageValid(image.getBytes())) {
                    return ResponseEntity.badRequest().build();
                }
            }
            // 3. 遍历图片字节数组调用业务层批量检测
            List<PestDetectionResponse> responseList = pestDetectionService.batchDetectPestsByImages(images);
            // 4. 返回批量识别结果
            return ResponseEntity.ok(responseList);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 查询害虫信息
     * 支持模糊查询，提高用户检索体验
     * @param pestName 害虫名称
     * @return 害虫详细信息，包括诊断结果及建议防治措施
     */
    @GetMapping("/pest-info")
    public ResponseEntity<PestDetectionResponse> queryPestInfoByName(@RequestParam("pestName") String pestName) {
        // 1. 参数校验
        if (!StringUtils.hasText(pestName) || pestName.trim().length() > 50) {
            return ResponseEntity.badRequest().build();
        }
        try {
            // 2. 调用业务层查询害虫详细信息（模糊匹配）
            PestDetectionResponse pestInfo = pestDetectionService.queryPestInfoByName(pestName.trim());
            if (pestInfo == null) {
                // 未找到对应害虫信息
                return ResponseEntity.ok(new PestDetectionResponse("未找到对应害虫信息"));
            }
            // 3. 返回害虫详细诊断结果及防治措施
            return ResponseEntity.ok(pestInfo);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
