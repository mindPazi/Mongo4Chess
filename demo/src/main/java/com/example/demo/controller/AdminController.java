package com.example.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.demo.service.PlayerService;
import com.example.demo.service.MatchService;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

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

    @Operation(summary = "Delete a player", description = "Deletes a player by username")
    @DeleteMapping("/player/username")
    public ResponseEntity<String> deletePlayer(@RequestBody String username) {
        playerService.deletePlayer(username);
        return ResponseEntity.ok("Player " + username + " deleted!");
    }

    @Operation(summary = "Ban a player", description = "Bans a player by username")
    @PatchMapping("/player/ban/player/{username}")
    public ResponseEntity<String> banPlayer(@RequestBody String username) {
        playerService.banPlayer(username);
        return ResponseEntity.ok("Player " + username + " banned!");
    }

    @Operation(summary = "Unban a player", description = "Unbans a player by username")
    @PatchMapping("/player/unban/player/{username}")
    public ResponseEntity<String> unbanPlayer(@RequestBody String username) {
        playerService.unBanPlayer(username);
        return ResponseEntity.ok("Player " + username + " unbanned!");
    }

    @Operation(summary = "Update admin username", description = "Updates the admin's username")
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

    @Operation(summary = "Update admin password", description = "Updates the admin's password")
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

    @Operation(summary = "Save a match", description = "Saves a new match to the database")
    @PostMapping("/match")
    public ResponseEntity<String> saveMatch(@RequestBody Match match) {
        matchService.saveMatch(match);
        return ResponseEntity.ok("Match saved successfully!");
    }

    @Operation(summary = "Delete all matches", description = "Deletes all matches from the database")
    @DeleteMapping("/matches")
    public ResponseEntity<String> deleteAllMatches() {
        matchService.deleteAllMatches();
        return ResponseEntity.ok("All matches deleted!");
    }

    @Operation(summary = "Delete matches by player", description = "Deletes all matches played by a specific player")
    @DeleteMapping("/matches/player/{username}")
    public ResponseEntity<String> deleteAllMatchesByPlayer(@RequestBody String username) {
        matchService.deleteAllMatchesByPlayer(username);
        return ResponseEntity.ok("All matches for player " + username + " deleted!");
    }

    @Operation(summary = "Add winner to tournament", description = "Adds a winner to the specified tournament")
    @PatchMapping("/tournament/{id}/winner")
    public ResponseEntity<String> addWinner(@PathVariable String id, @RequestParam String winner) {
        tournamentService.addWinner(id, winner);
        return ResponseEntity.ok("Winner " + winner + " added to tournament " + id);
    }

    @Operation(summary = "Get all tournaments", description = "Fetches all tournaments from the database")
    @GetMapping("/tournaments")
    public ResponseEntity<List<Tournament>> getTournaments() {
        return ResponseEntity.ok(tournamentService.getAllTournaments());
    }

    @Operation(summary = "Add player to tournament", description = "Adds a player to a specific tournament")
    @PatchMapping("/tournament/{tournamentId}/addPlayer")
    public ResponseEntity<String> addPlayerToTournament(@PathVariable String tournamentId,
            @RequestParam String playerId) {
        try {
            tournamentService.addPlayer(tournamentId, playerId);
            return ResponseEntity.ok("Player " + playerId + " added to tournament " + tournamentId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error adding player: " + e.getMessage());
        }
    }

    @Operation(summary = "Remove player from tournament", description = "Removes a player from a specific tournament")
    @PatchMapping("/tournament/{tournamentId}/removePlayer")
    public ResponseEntity<String> removePlayerFromTournament(@PathVariable String tournamentId,
            @RequestParam String playerId) {
        try {
            tournamentService.removePlayer(tournamentId, playerId);
            return ResponseEntity.ok("Player " + playerId + " removed from tournament " + tournamentId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error removing player: " + e.getMessage());
        }
    }

    @Operation(summary = "Add match to tournament", description = "Adds a match to a specific tournament")
    @PatchMapping("/tournament/{tournamentId}/addMatch")
    public ResponseEntity<String> addMatchToTournament(@PathVariable String tournamentId, @RequestBody Match match) {
        try {
            tournamentService.addMatch(tournamentId, match);
            return ResponseEntity.ok("Match added to tournament " + tournamentId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error adding match: " + e.getMessage());
        }
    }
}
