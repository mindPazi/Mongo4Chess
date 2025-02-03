package com.example.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.demo.service.PlayerService;
import com.example.demo.service.AdminService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final PlayerService playerService;
    private final AdminService adminService;

    @DeleteMapping("/delete_player")
    public ResponseEntity<String> deletePlayer(@RequestBody String username) {
        playerService.deletePlayer(username);
        return ResponseEntity.ok("Player " + username + " deleted!");
    }

    @PostMapping("/ban/player")
    public ResponseEntity<String> banPlayer(@RequestBody String username) {
        playerService.banPlayer(username);
        return ResponseEntity.ok("Player " + username + " banned!");
    }

    @PostMapping("/unban/player")
    public ResponseEntity<String> unbanPlayer(@RequestBody String username) {
        playerService.unBanPlayer(username);
        return ResponseEntity.ok("Player " + username + " unbanned!");
    }

    @PostMapping("/update/username")
    public ResponseEntity<String> updateAdminUsername(@RequestBody String username) {
        adminService.updateAdminUsername(username);
        return ResponseEntity.ok("Admin username updated!");
    }

    @PostMapping("/update/password")
    public ResponseEntity<String> updateAdminPassword(@RequestBody String password) {
        adminService.updateAdminPassword(password);
        return ResponseEntity.ok("Admin password updated!");

    }
}
