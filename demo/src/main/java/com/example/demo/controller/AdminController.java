package com.example.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.demo.service.PlayerService;
import com.example.demo.service.AdminService;

import java.util.Map;

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
    public ResponseEntity<String> updateAdminUsername(@RequestBody Map<String, String> requestBody) {
        String oldUsername = requestBody.get("oldUsername");
        String newUsername = requestBody.get("newUsername");

        if (oldUsername == null || newUsername == null) {
            return ResponseEntity.badRequest().body("Missing oldUsername or newUsername");
        }

        adminService.updateAdminUsername(oldUsername, newUsername);
        return ResponseEntity.ok("Admin username updated from " + oldUsername + " to " + newUsername + "!");
    }

    @PostMapping("/update/password")
    public ResponseEntity<String> updateAdminPassword(@RequestBody Map<String, String> requestBody) {
        String username = requestBody.get("username");
        String newPassword = requestBody.get("newPassword");

        if (username == null || newPassword == null) {
            return ResponseEntity.badRequest().body("Missing username or newPassword");
        }

        adminService.updateAdminPassword(username, newPassword);
        return ResponseEntity.ok("Admin password updated for " + username + "!");
    }

}