package com.example.demo.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import com.example.demo.service.AdminService;

import java.util.Date;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private final AdminService adminService;

    @PostMapping("update/username")
    public ResponseEntity<String> updateAdminUsername(@RequestBody String username) {
        return ResponseEntity.ok(adminService.updateAdminUsername(username));
    }

    @PostMapping("update/password")
    public ResponseEntity<String> updateAdminPassword(@RequestBody String password) {
        return ResponseEntity.ok(adminService.updateAdminPassword(password));
    }

}
