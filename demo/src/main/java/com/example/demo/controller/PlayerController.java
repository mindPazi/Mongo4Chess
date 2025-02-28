package com.example.demo.controller;

import com.example.demo.DTO.TournamentDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.example.demo.model.Match;
import com.example.demo.service.PlayerService;
import com.example.demo.service.TournamentService;
import com.example.demo.model.Tournament;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

//todo: fare match dto, tournament dto
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
        if (match == null ||
                match.getWhite() == null || match.getWhite().isEmpty() ||
                match.getBlack() == null || match.getBlack().isEmpty() ||
                match.getMoves() == null || match.getMoves().isEmpty() ||
                match.getWhiteElo() == 0 ||
                match.getBlackElo() == 0) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Match non valido. Tutti i campi obbligatori devono essere presenti.");
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
    public ResponseEntity<?> createTournament(@RequestBody @Valid TournamentDTO tournamentDTO) {

        try {
            // Ottieni l'utente autenticato
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();

            // Copia i campi del DTO in un oggetto Tournament
            Tournament tournament = new Tournament();
            BeanUtils.copyProperties(tournamentDTO, tournament);
            tournament.setCreator(currentUsername);
            tournament.setIsClosed(false);

            // Crea il torneo
            return ResponseEntity.status(HttpStatus.CREATED).body(tournamentService.createTournament(tournament));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore durante la creazione del torneo:"+e.getMessage());
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
            @ApiResponse(responseCode = "400", description = "Dati non validi"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @PostMapping("/tournament/join")
    public ResponseEntity<String> joinTournament(
            @RequestParam @Parameter(description = "ID del torneo a cui unirsi", required = true) String tournamentId) {

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

    @Operation(summary = "Ottieni i tornei attivi filtrati per Elo del giocatore")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Elenco tornei attivi ottenuto con successo"),
            @ApiResponse(responseCode = "401", description = "Utente non autenticato"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/tournament/active")
    public ResponseEntity<List<Tournament>> getActiveTournaments() {
        try {
            List<Tournament> tournaments = tournamentService.getActiveTournaments();
            return ResponseEntity.ok(tournaments);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Ottieni i tornei creati da un giocatore")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Elenco tornei creati ottenuto con successo")
    })
    @GetMapping("/tournament/created")
    public ResponseEntity<String> getCreatedTournaments(@RequestParam String creator) {
        return ResponseEntity.ok(tournamentService.getCreatedTournaments(creator));
    }

    @Operation(summary = "Ottieni l'andamento dell'ELO di un giocatore")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Andamento ELO ottenuto con successo")
    })
    @GetMapping("/{playerId}/elo_trend")
    public ResponseEntity<List<Integer>> getEloTrend(@PathVariable String playerId) {
        List<Integer> eloTrend = playerService.getEloTrend(playerId);
        return ResponseEntity.ok(eloTrend);
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
