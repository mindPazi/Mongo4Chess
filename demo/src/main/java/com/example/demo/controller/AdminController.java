package com.example.demo.controller;

import com.example.demo.DTO.MatchDTO;
import com.example.demo.DTO.TournamentDTO;
import com.example.demo.DTO.TournamentMatchDTO;
import com.example.demo.DTO.TournamentPlayerDTO;
import com.example.demo.model.TournamentMatch;
import com.example.demo.model.TournamentPlayer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Past;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

//todo: estrarre i metodi in comune tra player e admin
//todo: aggiungere see most important match of tournament

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
@Tag(name = "Admin Controller", description = "Admin operations")
public class AdminController {

    private final PlayerService playerService;
    private final AdminService adminService;
    private final MatchService matchService;
    private final TournamentService tournamentService;

    @Operation(summary = "Aggiungi le posizioni ai tornei", description = "Aggiunge le posizioni ai tornei")
    @PatchMapping("/tournament/updatePositions/{tournamentId}")
    public ResponseEntity<String> updatePositions(@PathVariable String tournamentId, @RequestBody @Valid List<TournamentPlayerDTO> tournamentPlayerDTOs) {
        try {
            List<TournamentPlayer> tournamentPlayers = new ArrayList<>();
            for (TournamentPlayerDTO tournamentPlayerDTO : tournamentPlayerDTOs) {
                TournamentPlayer tp = new TournamentPlayer();
                BeanUtils.copyProperties(tournamentPlayerDTO, tp);
                tournamentPlayers.add(tp);
            }
            tournamentService.updatePositions(tournamentPlayers, tournamentId);
            return ResponseEntity.ok("Positions updated!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating positions: " + e.getMessage());
        }
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
    @PutMapping("/admin/username/{newUsername}")
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
    @PutMapping("/admin/password")
    public ResponseEntity<?> updateAdminPassword(@RequestBody String oldPassword,
                                                 @RequestBody String newPassword) {
        if (oldPassword == null || oldPassword.isEmpty() || newPassword == null || newPassword.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Dati non validi per l'aggiornamento della password");
        }
        try {
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
        try {
            Match match = new Match();
            BeanUtils.copyProperties(matchDTO, match);
            matchService.saveMatch(match);
            return ResponseEntity.status(HttpStatus.CREATED).body(match); // Restituisci l'oggetto Match creato (o un DTO di risposta)
        } catch (Exception e) {
            // Gestione degli errori
            System.out.println(e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @Operation(summary = "Delete all matches", description = "Deletes all matches from the database")
    @DeleteMapping("/matches")
    public ResponseEntity<String> deleteAllMatches() {
        matchService.deleteAllMatches();
        return ResponseEntity.ok("All matches deleted!");
    }

    // i match vengono eliminati solo dalla collection dei match, rimangono nei player per tenere la statistica dell'andamento dell'elo
    @Operation(summary="Delete matches before a date", description="Deletes all matches played before a specific date")
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
        try {
            // Ottieni l'utente autenticato
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();

            Tournament tournament = new Tournament();
            BeanUtils.copyProperties(tournamentDTO, tournament);
            tournament.setCreator(currentUsername);
            tournament.setIsClosed(false);

            return ResponseEntity.status(HttpStatus.CREATED).body(tournamentService.createTournament(tournament));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore durante la creazione del torneo:" + e.getMessage());
        }
    }

    @Operation(summary = "Add winner to tournament", description = "Adds a winner to the specified tournament")
    @PatchMapping("/tournament/winner/{id}/{winner}")
    public ResponseEntity<String> addWinner(@PathVariable String id, @PathVariable String winner) {
        try {
            tournamentService.addWinner(id, winner);
            return ResponseEntity.ok("Winner " + winner + " added to tournament " + id);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error adding winner: " + e.getMessage());
        }
    }

    @Operation(summary = "Get all tournaments", description = "Fetches all tournaments from the database")
    @GetMapping("/tournaments")
    public ResponseEntity<List<Tournament>> getTournaments() {
        return ResponseEntity.ok(tournamentService.getAllTournaments());
    }

    @Operation(summary = "Add player to tournament", description = "Adds a player to a specific tournament")
    @PatchMapping("/tournament/addPlayer/{tournamentId}/{playerId}")
    public ResponseEntity<String> addPlayerToTournament(@PathVariable String tournamentId,
                                                        @PathVariable String playerId) {
        try {
            tournamentService.addPlayer(tournamentId, playerId);
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

    //todo: aggiustare, probabilemtne il problema è che bisogna copiare il matchDTO in match con copyProperties
    @Operation(summary = "Add match to tournament", description = "Adds a match to a specific tournament")
    @PatchMapping("/tournament/{tournamentId}/addMatch")
    public ResponseEntity<String> addMatchToTournament(@PathVariable String tournamentId, @RequestBody List<TournamentMatchDTO> tournamentMatchDTOs) {
        try {
            List<TournamentMatch> matches = new ArrayList<>();
            for (TournamentMatchDTO matchDTO : tournamentMatchDTOs) {
                TournamentMatch match = new TournamentMatch();
                Match matchEntity = new Match();
                BeanUtils.copyProperties(matchDTO.getMatch(), matchEntity);
                match.setMatch(matchEntity);
                match.setMatchGrade(matchDTO.getMatchGrade());
                matches.add(match);
            }
            tournamentService.addMostImportantMatches(tournamentId, matches);
            return ResponseEntity.ok("Match added to tournament " + tournamentId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error adding match: " + e.getMessage());
        }
    }
}
