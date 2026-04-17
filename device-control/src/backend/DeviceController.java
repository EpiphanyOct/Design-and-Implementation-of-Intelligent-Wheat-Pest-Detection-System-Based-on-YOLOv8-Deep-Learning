package com.topwheat.pestdetect.controller;

import com.topwheat.pestdetect.model.Device;
import com.topwheat.pestdetect.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 设备控制器
 * Feature: Device Control
 */
@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    @GetMapping("/list")
    public ResponseEntity<?> getDeviceList(
            @RequestParam(value = "deviceType", required = false) String deviceType,
            @RequestParam(value = "deviceName", required = false) String deviceName) {
        
        List<Device> devices = deviceService.findDevices(deviceType, deviceName);
        return ResponseEntity.ok(devices);
    }

    @PostMapping("/add")
    public ResponseEntity<?> addDevice(@RequestBody Device device) {
        boolean success = deviceService.addDevice(device);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateDevice(
            @PathVariable("id") String deviceId,
            @RequestBody Device device) {
        device.setDeviceId(deviceId);
        boolean success = deviceService.updateDevice(device);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteDevice(@PathVariable("id") String deviceId) {
        boolean success = deviceService.deleteDevice(deviceId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/control")
    public ResponseEntity<?> controlDevice(@RequestBody Map<String, Object> params) {
        String deviceId = (String) params.get("deviceId");
        String action = (String) params.get("action");
        
        boolean success = deviceService.controlDevice(deviceId, action, params);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/status/{id}")
    public ResponseEntity<?> getDeviceStatus(@PathVariable("id") String deviceId) {
        Map<String, Object> status = deviceService.getDeviceStatus(deviceId);
        return ResponseEntity.ok(status);
    }
}
