package com.example.demo.controller;

import com.example.demo.DTO.MatchDTO;
import com.example.demo.DTO.TournamentDTO;
import com.example.demo.DTO.TournamentMatchDTO;
import com.example.demo.DTO.TournamentPlayerDTO;
import com.example.demo.model.*;
import com.example.demo.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.example.demo.service.PlayerService;
import com.example.demo.service.TournamentService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import com.example.demo.service.MatchService;

//todo: delete friend
//todo: continuare a testare le get
//@RequiredArgsConstructor
//todo: spostare i metodi in comune in CommonPlayerAdminController
@RestController
@RequestMapping("/api/player")
@Tag(name = "Player Controller", description = "Player operations")
public class PlayerController extends CommonPlayerAdminController {

//    @Autowired
//    private MatchService matchService;
//
//    @Autowired
//    private PlayerService playerService;
//
//    @Autowired
//    private TournamentService tournamentService;

    PlayerController(MatchService matchService, PlayerService playerService, AdminService adminService, TournamentService tournamentService) {
        super(playerService, matchService, adminService, tournamentService);
    }

    @Operation(summary = "Crea un nuovo match")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Match creato con successo"),
            @ApiResponse(responseCode = "400", description = "Richiesta non valida")
    })
    @PostMapping("/match")
    public ResponseEntity<?> saveMatch(@RequestBody @Valid MatchDTO matchDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        if (!matchDTO.getWhite().equals(currentUsername) && !matchDTO.getBlack().equals(currentUsername)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Devi essere uno dei giocatori per salvare un match");
        }
        return super.saveMatch(matchDTO);
    }

    @Operation(summary = "Ottieni i tornei giocati dal giocatore")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Posizioni ottenute con successo"),
            @ApiResponse(responseCode = "400", description = "Richiesta non valida")
    })
    @GetMapping("/tournament/myTournaments")
    public ResponseEntity<?> getMyTournaments() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String playerId = authentication.getName();
            return ResponseEntity.ok(playerService.getMyTournaments(playerId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Errore durante il recupero delle posizioni: " + e.getMessage());
        }
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
        if (tournamentId == null || tournamentId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ID torneo non valido");
        }
        try {
            tournamentService.deleteTournament(tournamentId);
            return ResponseEntity.ok("Torneo eliminato con successo");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @Operation(summary = "Ottieni statistiche del giocatore")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statistiche ottenute con successo")
    })
    @GetMapping("/stats/{playerId}")
    public ResponseEntity<?> getStats(@PathVariable String playerId) {
        try {
            return ResponseEntity.ok(playerService.getStats(playerId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore durante il recupero delle statistiche: " + e.getMessage());
        }
    }

    @Operation(summary = "Aggiorna il nome utente del giocatore")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Username aggiornato con successo"),
            @ApiResponse(responseCode = "400", description = "Dati non validi")
    })
    @PutMapping("/username")
    public ResponseEntity<?> updatePlayerUsername(@RequestParam String new_username) {
        if (new_username == null || new_username.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Dati non validi per l'aggiornamento dello username");
        }
        try {
            playerService.updatePlayerUsername(new_username);
            return ResponseEntity.ok("Username aggiornato con successo");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore durante l'aggiornamento dello username");
        }
    }

    @Operation(summary = "Aggiorna la password del giocatore")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password aggiornata con successo"),
            @ApiResponse(responseCode = "400", description = "Dati non validi")
    })
    @PutMapping("/password")
    public ResponseEntity<?> updatePlayerPassword(@RequestParam String old_password,
                                                  @RequestParam String new_password) {
        if (old_password == null || old_password.isEmpty() || new_password == null || new_password.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Dati non validi per l'aggiornamento della password");
        }
        try {
            playerService.updatePlayerPassword(old_password, new_password);
            return ResponseEntity.ok("Password aggiornata con successo");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore durante l'aggiornamento della password");
        }
    }

    @Operation(summary = "Add match to tournament", description = "Adds a match to a specific tournament")
    @PatchMapping("/tournament/{tournamentId}/addMatch")
    public ResponseEntity<String> addMatchToTournament(@PathVariable String tournamentId, @RequestBody List<TournamentMatchDTO> matchDTOs) {
        try {
            List<TournamentMatch> matches = new ArrayList<>();
            for (TournamentMatchDTO matchDTO : matchDTOs) {
                TournamentMatch match = new TournamentMatch();
                BeanUtils.copyProperties(matchDTO, match);
                matches.add(match);
            }
            tournamentService.addMostImportantMatches(tournamentId, matches);
            return ResponseEntity.ok("Match added to tournament " + tournamentId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error adding match: " + e.getMessage());
        }
    }

    @Operation(summary = "Un giocatore si unisce a un torneo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Giocatore aggiunto al torneo"),
            @ApiResponse(responseCode = "400", description = "Dati non validi"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @PatchMapping("/tournament/join/{tournamentId}")
    public ResponseEntity<String> joinTournament(@PathVariable String tournamentId) {

        if (tournamentId == null || tournamentId.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Dati non validi per unirsi al torneo");
        }

        try {
            tournamentService.joinTournament(tournamentId);
            return ResponseEntity.ok("Giocatore aggiunto al torneo");
        } catch (IllegalArgumentException e) {
            // Per errori noti, come torneo non trovato
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            // Per errori di sistema o sconosciuti
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore durante l'aggiunta al torneo: " + e.getMessage());
        }
    }

    @Operation(summary = "Aggiungi le posizioni ai tornei", description = "Aggiunge le posizioni ai tornei")
    @PatchMapping("/tournament/updatePositions/{tournamentId}")
    public ResponseEntity<String> updatePositions(@PathVariable String tournamentId, @RequestBody @Valid List<TournamentPlayerDTO> tournamentPlayerDTOs) {
        return super.updatePositions(tournamentId, tournamentPlayerDTOs);
    }

    @Operation(summary = "Ottieni tutti i tornei")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Elenco tornei ottenuto con successo")
    })
    @GetMapping("/tournament/all")
    public ResponseEntity<?> getAllTournaments() {
        return super.getAllTournaments();
    }

    @Operation(summary = "Ottieni i tornei attivi filtrati per Elo del giocatore")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Elenco tornei attivi ottenuto con successo"),
            @ApiResponse(responseCode = "401", description = "Utente non autenticato"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/tournament/active")
    public ResponseEntity<?> getActiveTournaments() {
        try {
            List<Tournament> tournaments = tournamentService.getActiveTournaments();
            return ResponseEntity.ok(tournaments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore durante il recupero dei tornei attivi: " + e.getMessage());
        }
    }

    @Operation(summary = "Ottieni i tornei creati da un giocatore")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Elenco tornei creati ottenuto con successo")
    })
    @GetMapping("/tournament/created/{creator}")
    public ResponseEntity<List<Tournament>> getCreatedTournaments(@PathVariable String creator) {
        return ResponseEntity.ok(tournamentService.getCreatedTournaments(creator));
    }

    @Operation(summary = "Ottieni l'andamento dell'ELO di un giocatore")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Andamento ELO ottenuto con successo")
    })
    @GetMapping("/eloTrend/{playerId}")
    public ResponseEntity<?> getEloTrend(@PathVariable String playerId) {
        try {
            List<PlayerMatch> eloTrend = playerService.getEloTrend(playerId);
            return ResponseEntity.ok(eloTrend);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore durante il recupero dell'andamento dell'ELO: " + e.getMessage());
        }
    }

    @Operation(summary = "Aggiungi un amico alla lista amici del giocatore")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Amico aggiunto con successo"),
            @ApiResponse(responseCode = "400", description = "Dati non validi")
    })
    @PostMapping("/add_friend")
    public ResponseEntity<?> addFriend(@RequestParam String friendId) {
        if (friendId == null || friendId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ID amico non valido");
        }

        try {
            // Passa solo friendId al Service
            playerService.addFriend(friendId);
            return ResponseEntity.ok("Amico aggiunto con successo");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore durante l'aggiunta dell'amico");
        }
    }

    @Operation(summary = "Rimuovi un amico dalla lista amici del giocatore")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Amico rimosso con successo"),
            @ApiResponse(responseCode = "404", description = "Amico non trovato"),
            @ApiResponse(responseCode = "400", description = "Dati non validi")
    })
    @DeleteMapping("/remove_friend")
    public ResponseEntity<?> removeFriend(@RequestParam String friendId) {
        if (friendId == null || friendId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ID amico non valido");
        }

        try {
            playerService.removeFriend(friendId);
            return ResponseEntity.ok("Amico rimosso con successo");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore durante la rimozione dell'amico");
        }
    }

}
