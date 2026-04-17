package com.topwheat.pestdetect.controller;

import com.topwheat.pestdetect.service.DataManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据管理控制器
 * Feature: Data Management
 */
@RestController
@RequestMapping("/api/data-management")
public class DataManagementController {

    @Autowired
    private DataManagementService dataManagementService;

    @GetMapping("/list")
    public ResponseEntity<?> getDataList(
            @RequestParam(value = "dataType", required = false) String dataType,
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        
        List<Map<String, Object>> data = dataManagementService.getDataList(
                dataType, startTime, endTime, page, pageSize);
        int total = dataManagementService.countData(dataType, startTime, endTime);
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", data);
        result.put("total", total);
        result.put("current", page);
        result.put("pageSize", pageSize);
        
        return ResponseEntity.ok(result);
    }

    @PostMapping("/export")
    public ResponseEntity<?> exportData(@RequestBody Map<String, Object> params) {
        String filePath = dataManagementService.exportData(params);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("filePath", filePath);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/backup")
    public ResponseEntity<?> backupData() {
        String backupPath = dataManagementService.backupData();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("backupPath", backupPath);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/restore")
    public ResponseEntity<?> restoreData(@RequestParam("backupPath") String backupPath) {
        boolean success = dataManagementService.restoreData(backupPath);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        return ResponseEntity.ok(result);
    }
}
