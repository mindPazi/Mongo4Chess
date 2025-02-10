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

    @PostMapping("/match")
    public ResponseEntity<Match> createMatch(@RequestBody Match match) {
        matchService.saveMatch(match);
        return ResponseEntity.ok(match);
    }

    @PostMapping("/tournament/create")
    public ResponseEntity<Tournament> createTournament(@RequestBody Tournament tournament) {
        tournamentService.createTournament(tournament);
        return ResponseEntity.ok(tournament);
    }

    @DeleteMapping("/tournament")

    public ResponseEntity<Tournament> deleteTournament(@RequestBody Tournament tournament) {
        tournamentService.deleteTournament(tournament);
        return ResponseEntity.ok(tournament);
    }

    @GetMapping("/stats")
    public ResponseEntity<String> getStats() {
        // get stats
        return ResponseEntity.ok("Stats");
    }

    @PutMapping("/username")
    public ResponseEntity<String> updateUsername(@RequestBody String username, @RequestBody String new_username) {
        playerService.updatePlayerUsername(username, new_username);
        return ResponseEntity.ok("Username updated");
    }

    @PutMapping("/password")
    public ResponseEntity<String> updatePassword(@RequestBody String old_password, @RequestBody String new_password) {
        playerService.updatePlayerPassword(old_password, new_password);
        return ResponseEntity.ok("Password updated");
    }

    @PostMapping("/most_important_matches") // add most important matches to a tournament
    public ResponseEntity<String> addMostImportantMatches(@RequestBody List<Match> matches,
            @RequestBody String tournamentId) {
        tournamentService.addMostImportantMatches(matches, tournamentId);
        return ResponseEntity.ok("Matches added");
    }

    @PostMapping("/tournament/join")

    public ResponseEntity<String> joinTournament(String tournamentId, String playerUsername) {
        tournamentService.joinTournament(tournamentId, playerUsername);
        return ResponseEntity.ok("Player joined tournament");
    }

    @GetMapping("/tournament/all")
    public ResponseEntity<List<Tournament>> getAllTournaments() {
        return ResponseEntity.ok(tournamentService.getAllTournaments());
    }

    @GetMapping("/tournament/active")
    public ResponseEntity<List<Tournament>> getActiveTournaments() {
        return ResponseEntity.ok(tournamentService.getActiveTournaments());
    }

    // see tournaments created by a player

    @GetMapping("/tournament/created")
    public ResponseEntity<String> getCreatedTournaments(@RequestBody String creator) {
        return ResponseEntity.ok(tournamentService.getCreatedTournaments(creator));
    }

    @GetMapping("/{playerId}/elo_trend")
    public ResponseEntity<String> getEloTrend(@PathVariable String playerId) {
        return ResponseEntity.ok(playerService.getEloTrend(playerId));
    }

    @PostMapping("/{playerId}/add_friend")
    public ResponseEntity<String> addFriend(@PathVariable String playerId, @RequestBody String friendId) {
        playerService.addFriend(playerId, friendId);
        return ResponseEntity.ok("Friend added");
    }

    @DeleteMapping("/{playerId}/remove_friend")
    public ResponseEntity<String> removeFriend(@PathVariable String playerId, @RequestBody String friendId) {
        playerService.removeFriend(playerId, friendId);
        return ResponseEntity.ok("Friend removed");
    }
    // implementare i metodi per admin e player capendo come gestire
    // l'autenticazione
}
