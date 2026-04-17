package com.topwheat.pestdetect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 小麦害虫检测系统 - 主应用入口
 * 
 * @author TopWheat
 * @version 1.0.0
 */
@SpringBootApplication
public class WheatPestDetectionApplication {

    public static void main(String[] args) {
        SpringApplication.run(WheatPestDetectionApplication.class, args);
    }
}
