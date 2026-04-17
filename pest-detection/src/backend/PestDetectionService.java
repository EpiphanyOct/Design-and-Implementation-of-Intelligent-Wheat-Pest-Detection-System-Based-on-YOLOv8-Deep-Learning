package com.topwheat.pestdetect.service;

import com.topwheat.pestdetect.model.PestDetectionRecord;
import com.topwheat.pestdetect.model.PestDetectionResponse;
import com.topwheat.pestdetect.model.PestInfo;
import com.topwheat.pestdetect.repository.PestDetectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 害虫检测服务层
 * Feature: Pest Detection
 */
@Service
public class PestDetectionService {

    @Autowired
    private PestDetectionRepository pestDetectionRepository;

    // 害虫数据库（模拟）
    private final List<PestInfo> pestDatabase = new ArrayList<>();

    public PestDetectionService() {
        initPestDatabase();
    }

    private void initPestDatabase() {
        pestDatabase.add(createPestInfo("P001", "红蜘蛛", 
            "寄生于小麦叶片，取食汁液导致叶片枯黄",
            Arrays.asList("定期检查叶片，发现早期虫害立即喷药", 
                         "施用生物农药减少害虫数量",
                         "保证田间通风和采光，提高小麦抗性")));
        
        pestDatabase.add(createPestInfo("P002", "麦蚜",
            "吸取小麦汁液，导致叶片卷曲，严重时影响生长",
            Arrays.asList("使用蓝色诱捕器诱捕麦蚜",
                         "及时喷洒杀虫剂减少蚜虫数量",
                         "保持田间干净，减少蚜虫孳生环境")));
        
        pestDatabase.add(createPestInfo("P003", "小麦螟虫",
            "幼虫蛀食小麦茎秆，导致枯杆倒伏",
            Arrays.asList("加强田间管理，清除杂草杂物",
                         "适时播种，错开螟虫高发期",
                         "喷施地面杀虫剂防止产卵")));
        
        pestDatabase.add(createPestInfo("P004", "小麦吸浆虫",
            "幼虫吸食麦粒浆液，导致麦粒空瘪",
            Arrays.asList("深耕土壤，减少越冬虫源",
                         "成虫羽化期及时喷药",
                         "选用抗虫品种")));
    }

    private PestInfo createPestInfo(String id, String name, String desc, List<String> tips) {
        PestInfo pest = new PestInfo();
        pest.setPestId(id);
        pest.setName(name);
        pest.setDescription(desc);
        pest.setSuggestedPrevention(String.join("\n", tips));
        return pest;
    }

    /**
     * 单张图片害虫检测
     */
    public PestDetectionResponse detectPestByImage(byte[] imageBytes) {
        // 模拟害虫识别（实际项目中应调用AI模型）
        Random random = new Random(imageBytes.length + System.currentTimeMillis());
        
        // 随机选择害虫
        PestInfo detectedPest = pestDatabase.get(random.nextInt(pestDatabase.size()));
        
        // 生成置信度 (0.6 - 0.95)
        double confidence = 0.6 + random.nextDouble() * 0.35;
        
        // 构建响应
        PestDetectionResponse response = new PestDetectionResponse();
        response.setSuccess(true);
        response.setMessage("检测成功");
        response.setPestType(detectedPest.getName());
        response.setConfidence(confidence);
        response.setDiagnosis(detectedPest.getDescription());
        response.setSuggestions(detectedPest.getSuggestedPrevention());
        response.setDetectTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        // 构建识别结果列表
        List<PestDetectionResponse.IdentifiedPest> pests = new ArrayList<>();
        pests.add(createIdentifiedPest(detectedPest, confidence));
        response.setIdentifiedPests(pests);
        
        // 保存检测记录
        saveDetectionRecord(response);
        
        return response;
    }

    /**
     * 批量害虫检测
     */
    public List<PestDetectionResponse> batchDetectPestsByImages(List<MultipartFile> images) {
        List<PestDetectionResponse> results = new ArrayList<>();
        
        for (MultipartFile image : images) {
            try {
                PestDetectionResponse result = detectPestByImage(image.getBytes());
                results.add(result);
            } catch (Exception e) {
                PestDetectionResponse error = new PestDetectionResponse();
                error.setSuccess(false);
                error.setMessage("检测失败: " + e.getMessage());
                results.add(error);
            }
        }
        
        return results;
    }

    /**
     * 查询检测历史
     */
    public List<PestDetectionResponse> queryDetectionHistory(
            LocalDateTime startTime, LocalDateTime endTime, int page, int pageSize) {
        
        // 从数据库查询
        List<PestDetectionRecord> records;
        if (startTime != null && endTime != null) {
            records = pestDetectionRepository.findByUploadTimeBetween(
                    startTime, endTime, pageSize, (page - 1) * pageSize);
        } else {
            records = pestDetectionRepository.findAllByPage(pageSize, (page - 1) * pageSize);
        }
        
        return records.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 根据名称查询害虫信息
     */
    public PestDetectionResponse queryPestInfoByName(String pestName) {
        List<PestInfo> matchedPests = pestDatabase.stream()
                .filter(p -> p.getName().contains(pestName))
                .collect(Collectors.toList());
        
        if (matchedPests.isEmpty()) {
            return null;
        }
        
        PestInfo pest = matchedPests.get(0);
        PestDetectionResponse response = new PestDetectionResponse();
        response.setSuccess(true);
        response.setPestType(pest.getName());
        response.setDiagnosis(pest.getDescription());
        response.setSuggestions(pest.getSuggestedPrevention());
        
        return response;
    }

    /**
     * 获取支持的害虫类型
     */
    public List<String> getSupportedPestTypes() {
        return pestDatabase.stream()
                .map(PestInfo::getName)
                .collect(Collectors.toList());
    }

    // ============ 私有方法 ============

    private PestDetectionResponse.IdentifiedPest createIdentifiedPest(PestInfo pest, double confidence) {
        PestDetectionResponse.IdentifiedPest identifiedPest = new PestDetectionResponse.IdentifiedPest();
        identifiedPest.setPestId(pest.getPestId());
        identifiedPest.setPestName(pest.getName());
        identifiedPest.setConfidence(confidence);
        identifiedPest.setDescription(pest.getDescription());
        identifiedPest.setPreventionTips(Arrays.asList(pest.getSuggestedPrevention().split("\n")));
        return identifiedPest;
    }

    private void saveDetectionRecord(PestDetectionResponse response) {
        PestDetectionRecord record = new PestDetectionRecord();
        record.setPestType(response.getPestType());
        record.setConfidence(response.getConfidence());
        record.setDiagnosisResult(response.getDiagnosis());
        record.setPreventionAdvice(response.getSuggestions());
        record.setStatus("COMPLETED");
        pestDetectionRepository.save(record);
    }

    private PestDetectionResponse convertToResponse(PestDetectionRecord record) {
        PestDetectionResponse response = new PestDetectionResponse();
        response.setSuccess(true);
        response.setPestType(record.getPestType());
        response.setConfidence(record.getConfidence());
        response.setDiagnosis(record.getDiagnosisResult());
        response.setSuggestions(record.getPreventionAdvice());
        response.setDetectTime(record.getUploadTime().toString());
        return response;
    }
}
