package com.topwheat.pestdetect.dao;

import com.topwheat.pestdetect.model.PestDetectionRecord;
import com.topwheat.pestdetect.model.PestInfo;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

/**
 * DAO层：小麦害虫检测系统数据访问对象
 * 支持图片上传分析记录、害虫种类识别结果查询、虫害诊断结果保存及查询
 */
@Repository
@Transactional
public class WheatPestDetectionDAO {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * 插入一条害虫检测记录（包含图片路径/编码、上传时间、识别结果）
     * 图片上传后，系统对图片进行分析并识别害虫种类，保存检测记录及诊断结果和置信度
     * @param record 害虫检测记录实体
     * @return 插入成功返回true，失败返回false
     */
    public boolean insertPestDetectionRecord(PestDetectionRecord record) {
        try {
            entityManager.persist(record);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 支持分页，为业务层提供一定灵活的查询能力
     * @param startDate 查询起始时间（包含）
     * @param endDate 查询结束时间（包含）
     * @param offset 分页偏移量，起始行号（0开始）
     * @param limit 单次查询最大行数
     * @return 符合时间段的害虫检测记录集合
     */
    public List<PestDetectionRecord> queryPestDetectionRecordsByTime(Date startDate, Date endDate, int offset, int limit) {
        String jpql = "SELECT r FROM PestDetectionRecord r WHERE r.uploadTime BETWEEN :startDate AND :endDate ORDER BY r.uploadTime DESC";
        TypedQuery<PestDetectionRecord> query = entityManager.createQuery(jpql, PestDetectionRecord.class);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    /**
     * 查询害虫种类信息列表
     * 主要用于识别后返回的基础害虫种类名称及描述
     * @return 害虫信息列表
     */
    public List<PestInfo> queryAllPestInfo() {
        String jpql = "SELECT p FROM PestInfo p ORDER BY p.name ASC";
        TypedQuery<PestInfo> query = entityManager.createQuery(jpql, PestInfo.class);
        return query.getResultList();
    }

    /**
     * 根据害虫类型查询防治建议
     * @param pestType 害虫种类名称
     * @return 建议防治措施内容，未找到返回null
     */
    public String queryPreventionAdviceByPestType(String pestType) {
        String jpql = "SELECT p.suggestedPrevention FROM PestInfo p WHERE p.name = :pestType";
        TypedQuery<String> query = entityManager.createQuery(jpql, String.class);
        query.setParameter("pestType", pestType);
        List<String> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * 根据ID查询检测记录
     * @param id 记录ID
     * @return 检测记录详情，找不到返回null
     */
    public PestDetectionRecord queryPestDetectionRecordById(Long id) {
        return entityManager.find(PestDetectionRecord.class, id);
    }

    /**
     * 更新害虫检测记录的诊断结果及置信度，用于系统再次分析或人工复核后调整结果
     * @param id 记录ID
     * @param diagnosisResult 诊断结果描述
     * @param confidence 置信度分数(0~1)
     * @param preventionAdvice 建议防治措施
     * @return 更新成功返回true，失败返回false
     */
    public boolean updateDiagnosisResult(Long id, String diagnosisResult, double confidence, String preventionAdvice) {
        try {
            PestDetectionRecord record = entityManager.find(PestDetectionRecord.class, id);
            if (record == null) {
                return false;
            }
            record.setDiagnosisResult(diagnosisResult);
            record.setConfidence(confidence);
            record.setPreventionAdvice(preventionAdvice);
            entityManager.merge(record);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 新增害虫信息
     * @param pestInfo 害虫信息
     * @return 新增成功返回true
     */
    public boolean addPestInfo(PestInfo pestInfo) {
        try {
            entityManager.persist(pestInfo);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据名称模糊查询害虫
     * @param name 害虫名称关键词
     * @return 害虫信息列表
     */
    public List<PestInfo> findPestsByName(String name) {
        String jpql = "SELECT p FROM PestInfo p WHERE p.name LIKE :name";
        TypedQuery<PestInfo> query = entityManager.createQuery(jpql, PestInfo.class);
        query.setParameter("name", "%" + name + "%");
        return query.getResultList();
    }

    /**
     * 分页查询所有害虫
     * @param offset 偏移量
     * @param limit 每页数量
     * @return 害虫信息列表
     */
    public List<PestInfo> findAllPests(int offset, int limit) {
        String jpql = "SELECT p FROM PestInfo p ORDER BY p.id ASC";
        TypedQuery<PestInfo> query = entityManager.createQuery(jpql, PestInfo.class);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    /**
     * 根据ID查询害虫
     * @param id 害虫ID
     * @return 害虫信息
     */
    public PestInfo findPestById(int id) {
        return entityManager.find(PestInfo.class, id);
    }

    /**
     * 更新害虫信息
     * @param pestInfo 害虫信息
     * @return 更新成功返回true
     */
    public boolean updatePestInfo(PestInfo pestInfo) {
        try {
            entityManager.merge(pestInfo);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除害虫信息
     * @param id 害虫ID
     * @return 删除成功返回true
     */
    public boolean deletePestInfo(int id) {
        try {
            PestInfo pest = entityManager.find(PestInfo.class, id);
            if (pest != null) {
                entityManager.remove(pest);
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取害虫总数
     * @return 害虫数量
     */
    public int getTotalPestCount() {
        String jpql = "SELECT COUNT(p) FROM PestInfo p";
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        return query.getSingleResult().intValue();
    }
}
