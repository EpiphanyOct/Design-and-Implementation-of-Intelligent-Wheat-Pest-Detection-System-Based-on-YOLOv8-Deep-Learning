package com.topwheat.pestdetect.model;

import lombok.Data;
import javax.persistence.*;
import java.util.Date;

/**
 * 害虫信息实体类
 * 对应系统小麦害虫检测系统数据模型
 */
@Data
@Entity
@Table(name = "pest_info")
public class PestInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "pest_id", unique = true, length = 32)
    private String pestId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "scientific_name", length = 100)
    private String scientificName;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "feature_description", length = 2000)
    private String featureDescription;

    @Column(name = "harm_characteristics", length = 2000)
    private String harmCharacteristics;

    @Column(name = "control_method", length = 2000)
    private String controlMethod;

    @Column(name = "occurrence_pattern", length = 2000)
    private String occurrencePattern;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "common_symptoms", length = 1000)
    private String commonSymptoms;

    @Column(name = "suggested_prevention", length = 2000)
    private String suggestedPrevention;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }
}
