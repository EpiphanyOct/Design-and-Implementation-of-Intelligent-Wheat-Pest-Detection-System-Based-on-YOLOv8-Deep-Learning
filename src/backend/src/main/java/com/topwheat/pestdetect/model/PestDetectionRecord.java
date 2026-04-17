package com.topwheat.pestdetect.model;

import lombok.Data;
import javax.persistence.*;
import java.util.Date;

/**
 * 害虫检测记录实体类
 */
@Data
@Entity
@Table(name = "pest_detection_record")
public class PestDetectionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_path")
    private String imagePath;

    @Column(name = "upload_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date uploadTime;

    @Column(name = "pest_type")
    private String pestType;

    @Column(name = "confidence")
    private Double confidence;

    @Column(name = "diagnosis_result", length = 1000)
    private String diagnosisResult;

    @Column(name = "prevention_advice", length = 2000)
    private String preventionAdvice;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "status")
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
