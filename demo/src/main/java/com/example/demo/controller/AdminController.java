package com.example.demo.controller;

import com.example.demo.DTO.*;
import com.example.demo.model.TournamentMatch;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Past;

import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.demo.service.PlayerService;
import com.example.demo.service.MatchService;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import com.example.demo.model.Match;
import com.example.demo.model.Tournament;
import com.example.demo.service.AdminService;
import com.example.demo.service.TournamentService;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin Controller", description = "Admin operations")
public class AdminController extends CommonPlayerAdminController {
    public AdminController(PlayerService playerService, MatchService matchService, AdminService adminService, TournamentService tournamentService) {
        super(playerService, matchService, adminService, tournamentService);
    }

    @Operation(summary = "Aggiungi le posizioni ai tornei", description = "Aggiunge le posizioni ai tornei")
    @PatchMapping("/tournament/updatePositions/{tournamentId}")
    public ResponseEntity<String> updatePositions(@PathVariable String tournamentId, @RequestBody @Valid List<TournamentPlayerDTO> tournamentPlayerDTOs) {
        return super.updatePositions(tournamentId, tournamentPlayerDTOs);
    }

    @Operation(summary = "Delete a player", description = "Deletes a player by username")
    @DeleteMapping("/player/{username}")
    public ResponseEntity<String> deletePlayer(@PathVariable String username) {
        try {
            playerService.deletePlayer(username);
            return ResponseEntity.ok("Player " + username + " deleted!");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @Operation(summary = "Ban a player", description = "Bans a player by username")
    @PatchMapping("/player/ban/player/{username}")
    public ResponseEntity<String> banPlayer(@PathVariable String username) {
        try {
            playerService.banPlayer(username);
            return ResponseEntity.ok("Player " + username + " banned!");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @Operation(summary = "Unban a player", description = "Unbans a player by username")
    @PatchMapping("/player/unban/player/{username}")
    public ResponseEntity<String> unbanPlayer(@PathVariable String username) {
        try {
            playerService.unBanPlayer(username);
            return ResponseEntity.ok("Player " + username + " unbanned!");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @Operation(summary = "Aggiorna il nome utente dell'admin")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Username aggiornato con successo"),
            @ApiResponse(responseCode = "400", description = "Dati non validi")
    })
    @PutMapping("/username/{newUsername}")
    public ResponseEntity<?> updateAdminUsername(@PathVariable String newUsername) {
        if (newUsername == null || newUsername.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Dati non validi per l'aggiornamento dello username");
        }
        try {
            adminService.updateAdminUsername(newUsername);
            return ResponseEntity.ok("Username aggiornato con successo");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore durante l'aggiornamento dello username");
        }
    }

    @Operation(summary = "Aggiorna la password dell'admin")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password aggiornata con successo"),
            @ApiResponse(responseCode = "400", description = "Dati non validi")
    })
    @PutMapping("/password")
    public ResponseEntity<?> updateAdminPassword(@RequestBody @Valid UpdatePasswordDTO updatePasswordDTO) {
        try {
            String oldPassword = updatePasswordDTO.getOldPassword();
            String newPassword = updatePasswordDTO.getNewPassword();
            adminService.updateAdminPassword(oldPassword, newPassword);
            return ResponseEntity.ok("Password aggiornata con successo");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore durante l'aggiornamento della password");
        }
    }

    @Operation(summary = "Save a match", description = "Saves a new match to the database")
    @PostMapping("/match")
    public ResponseEntity<?> saveMatch(@RequestBody @Valid MatchDTO matchDTO) {
        return super.saveMatch(matchDTO);
    }

    @Operation(summary = "Delete all matches", description = "Deletes all matches from the database")
    @DeleteMapping("/matches")
    public ResponseEntity<String> deleteAllMatches() {
        matchService.deleteAllMatches();
        return ResponseEntity.ok("All matches deleted!");
    }

    // i match vengono eliminati solo dalla collection dei match, rimangono nei player per tenere la statistica dell'andamento dell'elo
    @Operation(summary = "Delete matches before a date", description = "Deletes all matches played before a specific date")
    @DeleteMapping("/matches/before/{date}")
    public ResponseEntity<String> deleteMatchesBeforeDate(@PathVariable @Valid @Past @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date date) {
        matchService.deleteMatchesBeforeDate(date);
        return ResponseEntity.ok("All matches before " + date + " deleted!");
    }

    @Operation(summary = "Delete matches by player", description = "Deletes all matches played by a specific player")
    @DeleteMapping("/matches/player/{username}")
    public ResponseEntity<String> deleteAllMatchesByPlayer(@PathVariable String username) {
        matchService.deleteAllMatchesByPlayer(username);
        return ResponseEntity.ok("All matches for player " + username + " deleted!");
    }

    @Operation(summary = "Crea un nuovo torneo")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Torneo creato con successo"),
            @ApiResponse(responseCode = "400", description = "Richiesta non valida")
    })
    @PostMapping("/tournament/create")
    public ResponseEntity<?> createTournament(@RequestBody @Valid TournamentDTO tournamentDTO) {
        return super.createTournament(tournamentDTO);
    }

    //un admin può eliminare qualsiasi torneo
    @Operation(summary = "Elimina un torneo")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Torneo eliminato con successo"),
            @ApiResponse(responseCode = "404", description = "Torneo non trovato")
    })
    @DeleteMapping("/tournament/delete/{tournamentId}")
    public ResponseEntity<?> deleteTournament(@PathVariable String tournamentId) {
        return super.deleteTournament(tournamentId);
    }

    @Operation(summary = "Get all tournaments", description = "Fetches all tournaments from the database")
    @GetMapping("/tournaments")
    public ResponseEntity<?> getAllTournaments() {
        return super.getAllTournaments();
    }

    @Operation(summary = "Get tournaments by date", description = "Get tournaments by date")
    @GetMapping("/tournaments/date/{startDate}/{endDate}")
    public ResponseEntity<?> getTournamentsByDate(@PathVariable @Valid @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate, @PathVariable @Valid @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {
        return super.getTournamentsByDate(startDate, endDate);
    }

    @Operation(summary = "Add player to tournament", description = "Adds a player to a specific tournament")
    @PatchMapping("/tournament/addPlayer/{tournamentId}/{playerId}")
    public ResponseEntity<String> addPlayerToTournament(@PathVariable String tournamentId,
                                                        @PathVariable String playerId) {
        try {
            tournamentService.addPlayerByAdmin(tournamentId, playerId);
            return ResponseEntity.ok("Player " + playerId + " added to tournament " + tournamentId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error adding player: " + e.getMessage());
        }
    }

    @Operation(summary = "Remove player from tournament", description = "Removes a player from a specific tournament")
    @PatchMapping("/tournament/removePlayer/{tournamentId}/{playerId}")
    public ResponseEntity<String> removePlayerFromTournament(@PathVariable String tournamentId,
                                                             @PathVariable String playerId) {
        try {
            tournamentService.removePlayer(tournamentId, playerId);
            return ResponseEntity.ok("Player " + playerId + " removed from tournament " + tournamentId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error removing player: " + e.getMessage());
        }
    }


    @Operation(summary = "Add match to tournament", description = "Adds a match to a specific tournament")
    @PatchMapping("/tournament/{tournamentId}/addMatch")
    public ResponseEntity<String> addMatchToTournament(@PathVariable String tournamentId, @RequestBody List<TournamentMatchDTO> tournamentMatchDTOs) {
        return super.addMatchToTournament(tournamentId, tournamentMatchDTOs);
    }
}
