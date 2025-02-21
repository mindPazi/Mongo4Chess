package com.example.demo.controller;

import lombok.RequiredArgsConstructor;
import com.example.demo.model.Match;
import com.example.demo.service.PlayerService;
import com.example.demo.service.TournamentService;
import com.example.demo.model.Tournament;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import com.example.demo.service.MatchService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/player")
@Tag(name = "Player Controller", description = "Player operations")
public class PlayerController {

    @Autowired
    private MatchService matchService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private TournamentService tournamentService;

    @Operation(summary = "Crea un nuovo match")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Match creato con successo"),
            @ApiResponse(responseCode = "400", description = "Richiesta non valida")
    })
    @PostMapping("/match")
    public ResponseEntity<?> createMatch(@RequestBody Match match) {
        if (match == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Match non valido");
        }
        try {
            matchService.saveMatch(match);
            return ResponseEntity.ok(match);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore durante la creazione del match");
        }
    }

    @Operation(summary = "Crea un nuovo torneo")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Torneo creato con successo"),
            @ApiResponse(responseCode = "400", description = "Richiesta non valida")
    })
    @PostMapping("/tournament/create")
    public ResponseEntity<?> createTournament(@RequestBody Tournament tournament) {
        if (tournament == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Torneo non valido");
        }
        try {
            tournamentService.createTournament(tournament);
            return ResponseEntity.status(HttpStatus.CREATED).body(tournament);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore durante la creazione del torneo");
        }
    }

    @Operation(summary = "Elimina un torneo")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Torneo eliminato con successo"),
            @ApiResponse(responseCode = "404", description = "Torneo non trovato")
    })
    @DeleteMapping("/tournament")
    public ResponseEntity<?> deleteTournament(@RequestBody String tournamentid) {
        if (tournamentid == null || tournamentid.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ID torneo non valido");
        }
        try {
            tournamentService.deleteTournament(tournamentid);
            return ResponseEntity.ok("Torneo eliminato con successo");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Torneo non trovato");
        }
    }

    @Operation(summary = "Ottieni statistiche del giocatore")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statistiche ottenute con successo")
    })
    @GetMapping("/stats")
    public ResponseEntity<String> getStats() {
        return ResponseEntity.ok("Stats");
    }

    @Operation(summary = "Aggiorna il nome utente del giocatore")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Username aggiornato con successo"),
            @ApiResponse(responseCode = "400", description = "Dati non validi")
    })
    @PutMapping("/username")
    public ResponseEntity<?> updateUsername(@RequestBody String username, @RequestBody String new_username) {
        if (username == null || username.isEmpty() || new_username == null || new_username.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Dati non validi per l'aggiornamento dello username");
        }
        try {
            playerService.updatePlayerUsername(username, new_username);
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
    public ResponseEntity<?> updatePassword(@RequestBody String old_password, @RequestBody String new_password) {
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

    @Operation(summary = "Aggiungi le partite più importanti a un torneo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Partite aggiunte con successo"),
            @ApiResponse(responseCode = "400", description = "Dati non validi")
    })
    @PostMapping("/most_important_matches")
    public ResponseEntity<?> addMostImportantMatches(@RequestBody List<Match> matches,
            @RequestBody String tournamentId) {
        if (matches == null || matches.isEmpty() || tournamentId == null || tournamentId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Dati non validi per aggiungere le partite");
        }
        try {
            tournamentService.addMostImportantMatches(matches, tournamentId);
            return ResponseEntity.ok("Partite aggiunte con successo");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore durante l'aggiunta delle partite");
        }
    }

    @Operation(summary = "Un giocatore si unisce a un torneo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Giocatore aggiunto al torneo"),
            @ApiResponse(responseCode = "400", description = "Dati non validi")
    })
    @PostMapping("/tournament/join")
    public ResponseEntity<?> joinTournament(@RequestParam String tournamentId, @RequestParam String playerUsername) {
        if (tournamentId == null || tournamentId.isEmpty() || playerUsername == null || playerUsername.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Dati non validi per unirsi al torneo");
        }
        try {
            tournamentService.joinTournament(tournamentId, playerUsername);
            return ResponseEntity.ok("Giocatore aggiunto al torneo");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore durante l'aggiunta al torneo");
        }
    }

    @Operation(summary = "Ottieni tutti i tornei")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Elenco tornei ottenuto con successo")
    })
    @GetMapping("/tournament/all")
    public ResponseEntity<List<Tournament>> getAllTournaments() {
        return ResponseEntity.ok(tournamentService.getAllTournaments());
    }

    @Operation(summary = "Ottieni i tornei attivi")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Elenco tornei attivi ottenuto con successo")
    })
    @GetMapping("/tournament/active")
    public ResponseEntity<List<Tournament>> getActiveTournaments() {
        return ResponseEntity.ok(tournamentService.getActiveTournaments());
    }

    @Operation(summary = "Ottieni i tornei creati da un giocatore")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Elenco tornei creati ottenuto con successo")
    })
    @GetMapping("/tournament/created")
    public ResponseEntity<String> getCreatedTournaments(@RequestBody String creator) {
        return ResponseEntity.ok(tournamentService.getCreatedTournaments(creator));
    }

    @Operation(summary = "Ottieni l'andamento dell'ELO di un giocatore")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Andamento ELO ottenuto con successo")
    })
    @GetMapping("/{playerId}/elo_trend")
    public ResponseEntity<String> getEloTrend(@PathVariable String playerId) {
        return ResponseEntity.ok(playerService.getEloTrend(playerId));
    }

    @Operation(summary = "Aggiungi un amico alla lista amici del giocatore")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Amico aggiunto con successo"),
            @ApiResponse(responseCode = "400", description = "Dati non validi")
    })
    @PostMapping("/{playerId}/add_friend")
    public ResponseEntity<?> addFriend(@PathVariable String playerId, @RequestBody String friendId) {
        if (playerId == null || playerId.isEmpty() || friendId == null || friendId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Dati non validi per aggiungere l'amico");
        }
        try {
            playerService.addFriend(playerId, friendId);
            return ResponseEntity.ok("Amico aggiunto con successo");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore durante l'aggiunta dell'amico");
        }
    }

    @Operation(summary = "Rimuovi un amico dalla lista amici del giocatore")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Amico rimosso con successo"),
            @ApiResponse(responseCode = "404", description = "Amico non trovato")
    })
    @DeleteMapping("/{playerId}/remove_friend")
    public ResponseEntity<?> removeFriend(@PathVariable String playerId, @RequestBody String friendId) {
        if (playerId == null || playerId.isEmpty() || friendId == null || friendId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Dati non validi per rimuovere l'amico");
        }
        try {
            playerService.removeFriend(playerId, friendId);
            return ResponseEntity.ok("Amico rimosso con successo");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Amico non trovato");
        }
    }
}
