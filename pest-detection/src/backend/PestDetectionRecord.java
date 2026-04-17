package com.topwheat.pestdetect.model;

import lombok.Data;
import javax.persistence.*;
import java.util.Date;

/**
 * 害虫检测记录实体类
 * Feature: Pest Detection
 */
@Data
@Entity
@Table(name = "pest_detection_record")
public class PestDetectionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", length = 32)
    private String userId;

    @Column(name = "image_path", length = 500)
    private String imagePath;

    @Column(name = "upload_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date uploadTime;

    @Column(name = "pest_type", length = 100)
    private String pestType;

    @Column(name = "confidence", precision = 5, scale = 4)
    private Double confidence;

    @Column(name = "diagnosis_result", length = 1000)
    private String diagnosisResult;

    @Column(name = "prevention_advice", length = 2000)
    private String preventionAdvice;

    @Column(name = "status", length = 20)
    private String status; // PENDING, COMPLETED, FAILED

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
        if (uploadTime == null) {
            uploadTime = new Date();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }
}
