package com.example.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.demo.service.PlayerService;
import com.example.demo.service.MatchService;

import io.swagger.v3.oas.annotations.tags.Tag;

import com.example.demo.model.Match;
import com.example.demo.model.Tournament;
import com.example.demo.service.AdminService;
import com.example.demo.service.TournamentService;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
@Tag(name = "Admin Controller", description = "Admin operations")
public class AdminController {

    private final PlayerService playerService;
    private final AdminService adminService;
    private final MatchService matchService;
    private final TournamentService tournamentService;

    @DeleteMapping("/player/username")
    public ResponseEntity<String> deletePlayer(@RequestBody String username) {
        playerService.deletePlayer(username);
        return ResponseEntity.ok("Player " + username + " deleted!");
    }

    @PatchMapping("/player/ban/player/{username}")
    public ResponseEntity<String> banPlayer(@RequestBody String username) {
        playerService.banPlayer(username);
        return ResponseEntity.ok("Player " + username + " banned!");
    }

    @PatchMapping("/player/unban/player/{username}")
    public ResponseEntity<String> unbanPlayer(@RequestBody String username) {
        playerService.unBanPlayer(username);
        return ResponseEntity.ok("Player " + username + " unbanned!");
    }

    @PutMapping("/username")
    public ResponseEntity<String> updateAdminUsername(@RequestBody Map<String, String> requestBody) {
        String oldUsername = requestBody.get("oldUsername");
        String newUsername = requestBody.get("newUsername");

        if (oldUsername == null || newUsername == null) {
            return ResponseEntity.badRequest().body("Missing oldUsername or newUsername");
        }

        adminService.updateAdminUsername(oldUsername, newUsername);
        return ResponseEntity.ok("Admin username updated from " + oldUsername + " to " + newUsername + "!");
    }

    @PutMapping("/password")
    public ResponseEntity<String> updateAdminPassword(@RequestBody Map<String, String> requestBody) {
        String username = requestBody.get("username");
        String newPassword = requestBody.get("newPassword");

        if (username == null || newPassword == null) {
            return ResponseEntity.badRequest().body("Missing username or newPassword");
        }

        adminService.updateAdminPassword(username, newPassword);
        return ResponseEntity.ok("Admin password updated for " + username + "!");
    }

    @PostMapping("/match")
    public ResponseEntity<String> saveMatch(@RequestBody Match match) {
        matchService.saveMatch(match);
        return ResponseEntity.ok("Match saved successfully!");
    }

    @DeleteMapping("/matches")
    public ResponseEntity<String> deleteAllMatches() {
        matchService.deleteAllMatches();
        return ResponseEntity.ok("All matches deleted!");
    }

    @DeleteMapping("/matches/player/{username}")
    public ResponseEntity<String> deleteAllMatchesByPlayer(@RequestBody String username) {
        matchService.deleteAllMatchesByPlayer(username);
        return ResponseEntity.ok("All matches for player " + username + " deleted!");
    }

    @PatchMapping("/tournament/{id}/winner")
    public ResponseEntity<String> addWinner(@PathVariable String id, @RequestParam String winner) {
        tournamentService.addWinner(id, winner);
        return ResponseEntity.ok("Winner " + winner + " added to tournament " + id);
    }

    @GetMapping("/tournaments")
    public ResponseEntity<List<Tournament>> getTournaments() {
        return ResponseEntity.ok(tournamentService.getAllTournaments());
    }

}