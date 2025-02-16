package com.example.demo.controller;

import lombok.RequiredArgsConstructor;
import com.example.demo.model.Match;
import com.example.demo.service.PlayerService;
import com.example.demo.service.TournamentService;
import com.example.demo.model.Tournament;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<Match> createMatch(@RequestBody Match match) {
        matchService.saveMatch(match);
        return ResponseEntity.ok(match);
    }

    @Operation(summary = "Crea un nuovo torneo")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Torneo creato con successo"),
            @ApiResponse(responseCode = "400", description = "Richiesta non valida")
    })
    @PostMapping("/tournament/create")
    public ResponseEntity<Tournament> createTournament(@RequestBody Tournament tournament) {
        tournamentService.createTournament(tournament);
        return ResponseEntity.ok(tournament);
    }

    @Operation(summary = "Elimina un torneo")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Torneo eliminato con successo"),
            @ApiResponse(responseCode = "404", description = "Torneo non trovato")
    })
    @DeleteMapping("/tournament")
    public ResponseEntity<Tournament> deleteTournament(@RequestBody Tournament tournament) {
        tournamentService.deleteTournament(tournament);
        return ResponseEntity.ok(tournament);
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
    public ResponseEntity<String> updateUsername(@RequestBody String username, @RequestBody String new_username) {
        playerService.updatePlayerUsername(username, new_username);
        return ResponseEntity.ok("Username updated");
    }

    @Operation(summary = "Aggiorna la password del giocatore")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password aggiornata con successo"),
            @ApiResponse(responseCode = "400", description = "Dati non validi")
    })
    @PutMapping("/password")
    public ResponseEntity<String> updatePassword(@RequestBody String old_password, @RequestBody String new_password) {
        playerService.updatePlayerPassword(old_password, new_password);
        return ResponseEntity.ok("Password updated");
    }

    @Operation(summary = "Aggiungi le partite più importanti a un torneo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Partite aggiunte con successo"),
            @ApiResponse(responseCode = "400", description = "Dati non validi")
    })
    @PostMapping("/most_important_matches")
    public ResponseEntity<String> addMostImportantMatches(@RequestBody List<Match> matches,
            @RequestBody String tournamentId) {
        tournamentService.addMostImportantMatches(matches, tournamentId);
        return ResponseEntity.ok("Matches added");
    }

    @Operation(summary = "Un giocatore si unisce a un torneo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Giocatore aggiunto al torneo"),
            @ApiResponse(responseCode = "400", description = "Dati non validi")
    })
    @PostMapping("/tournament/join")
    public ResponseEntity<String> joinTournament(String tournamentId, String playerUsername) {
        tournamentService.joinTournament(tournamentId, playerUsername);
        return ResponseEntity.ok("Player joined tournament");
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
    public ResponseEntity<String> addFriend(@PathVariable String playerId, @RequestBody String friendId) {
        playerService.addFriend(playerId, friendId);
        return ResponseEntity.ok("Friend added");
    }

    @Operation(summary = "Rimuovi un amico dalla lista amici del giocatore")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Amico rimosso con successo"),
            @ApiResponse(responseCode = "404", description = "Amico non trovato")
    })
    @DeleteMapping("/{playerId}/remove_friend")
    public ResponseEntity<String> removeFriend(@PathVariable String playerId, @RequestBody String friendId) {
        playerService.removeFriend(playerId, friendId);
        return ResponseEntity.ok("Friend removed");
    }
}
