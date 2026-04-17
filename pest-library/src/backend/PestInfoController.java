package com.topwheat.pestdetect.controller;

import com.topwheat.pestdetect.model.PestInfo;
import com.topwheat.pestdetect.service.PestInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 害虫信息控制器
 * Feature: Pest Library
 */
@RestController
@RequestMapping("/api/pest-info")
public class PestInfoController {

    @Autowired
    private PestInfoService pestInfoService;

    @GetMapping("/list")
    public ResponseEntity<?> getPestList(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        
        List<PestInfo> pests = pestInfoService.findPests(keyword, page, pageSize);
        int total = pestInfoService.countPests(keyword);
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", pests);
        result.put("total", total);
        result.put("current", page);
        result.put("pageSize", pageSize);
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<?> getPestDetail(@PathVariable("id") int id) {
        PestInfo pest = pestInfoService.findById(id);
        if (pest == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(pest);
    }

    @PostMapping("/add")
    public ResponseEntity<?> addPest(@RequestBody PestInfo pestInfo) {
        boolean success = pestInfoService.addPest(pestInfo);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updatePest(@PathVariable("id") int id, @RequestBody PestInfo pestInfo) {
        pestInfo.setId(id);
        boolean success = pestInfoService.updatePest(pestInfo);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deletePest(@PathVariable("id") int id) {
        boolean success = pestInfoService.deletePest(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        return ResponseEntity.ok(result);
    }
}
