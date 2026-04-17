package com.topwheat.pestdetect.service;

import com.topwheat.pestdetect.model.PestDetectionRecord;
import com.topwheat.pestdetect.model.PestDetectionResponse;
import com.topwheat.pestdetect.model.PestInfo;
import com.topwheat.pestdetect.dao.WheatPestDetectionDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 害虫检测服务层
 * 处理害虫识别的业务逻辑
 */
@Service
public class PestDetectionService {

    @Autowired
    private WheatPestDetectionDAO wheatPestDetectionDAO;

    // 害虫数据库
    private final List<PestInfo> pestDatabase = new ArrayList<>();

    public PestDetectionService() {
        initPestDatabase();
    }

    private void initPestDatabase() {
        PestInfo pest1 = new PestInfo();
        pest1.setPestId("P001");
        pest1.setName("红蜘蛛");
        pest1.setDescription("寄生于小麦叶片,取食汁液导致叶片枯黄");
        pest1.setSuggestedPrevention("1. 定期检查叶片，发现早期虫害立即喷药\n2. 施用生物农药减少害虫数量\n3. 保证田间通风和采光，提高小麦抗性");
        pestDatabase.add(pest1);

        PestInfo pest2 = new PestInfo();
        pest2.setPestId("P002");
        pest2.setName("麦蚜");
        pest2.setDescription("吸取小麦汁液，导致叶片卷曲，严重时影响生长");
        pest2.setSuggestedPrevention("1. 使用蓝色诱捕器诱捕麦蚜\n2. 及时喷洒杀虫剂减少蚜虫数量\n3. 保持田间干净，减少蚜虫孳生环境");
        pestDatabase.add(pest2);

        PestInfo pest3 = new PestInfo();
        pest3.setPestId("P003");
        pest3.setName("小麦螟虫");
        pest3.setDescription("幼虫蛀食小麦茎秆，导致枯杆倒伏");
        pest3.setSuggestedPrevention("1. 加强田间管理，清除杂草杂物\n2. 适时播种，错开螟虫高发期\n3. 喷施地面杀虫剂防止产卵");
        pestDatabase.add(pest3);
    }

    /**
     * 上传图片进行害虫识别
     * @param imageBytes 图片字节数组
     * @return 检测结果响应
     */
    public PestDetectionResponse detectPestByImage(byte[] imageBytes) {
        // 模拟害虫识别逻辑
        Random random = new Random(imageBytes.length + System.currentTimeMillis());
        
        // 随机选择一个害虫
        PestInfo detectedPest = pestDatabase.get(random.nextInt(pestDatabase.size()));
        
        // 生成置信度 (0.6 - 0.95)
        double confidence = 0.6 + random.nextDouble() * 0.35;
        
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
        PestDetectionResponse.IdentifiedPest pest = new PestDetectionResponse.IdentifiedPest();
        pest.setPestId(detectedPest.getPestId());
        pest.setPestName(detectedPest.getName());
        pest.setConfidence(confidence);
        pest.setDescription(detectedPest.getDescription());
        pest.setPreventionTips(Arrays.asList(detectedPest.getSuggestedPrevention().split("\n")));
        pests.add(pest);
        response.setIdentifiedPests(pests);
        
        return response;
    }

    /**
     * 查询检测历史
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 历史记录列表
     */
    public List<PestDetectionResponse> queryDetectionHistory(LocalDateTime startTime, LocalDateTime endTime) {
        // 这里应该从数据库查询真实数据
        List<PestDetectionResponse> history = new ArrayList<>();
        
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            PestInfo pest = pestDatabase.get(random.nextInt(pestDatabase.size()));
            PestDetectionResponse record = new PestDetectionResponse();
            record.setSuccess(true);
            record.setPestType(pest.getName());
            record.setConfidence(0.6 + random.nextDouble() * 0.35);
            record.setDiagnosis(pest.getDescription());
            record.setSuggestions(pest.getSuggestedPrevention());
            record.setDetectTime(LocalDateTime.now().minusDays(i).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            history.add(record);
        }
        
        return history;
    }

    /**
     * 批量害虫检测
     * @param images 图片列表
     * @return 批量检测结果
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
     * 根据名称查询害虫信息
     * @param pestName 害虫名称
     * @return 害虫信息
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
     * 获取所有害虫类型
     * @return 害虫类型列表
     */
    public List<String> getSupportedPestTypes() {
        return pestDatabase.stream()
                .map(PestInfo::getName)
                .collect(Collectors.toList());
    }
}
