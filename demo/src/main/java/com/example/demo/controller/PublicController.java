package com.example.demo.controller;

import com.example.demo.model.Match;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.demo.service.MatchService;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public")
@Tag(name = "Public", description = "Public Controller")
public class PublicController {

    private final MatchService matchService;

    @Operation(summary = "Get all matches", description = "Get all matches")
    @GetMapping("/matches")
    public ResponseEntity<?> getMatches() {
        try {
            List<Match> matches = matchService.getMatches();
            return ResponseEntity.ok(matches);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Get matches by date", description = "Get matches by date")
    @GetMapping("/matches/{startDate}/{endDate}")
    public ResponseEntity<?> getMatchesByDate(
            @PathVariable @Valid @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @PathVariable @Valid @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {
        try {
            List<Match> matches = matchService.getMatchesByDate(startDate, endDate);
            return ResponseEntity.ok(matches);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Get matches by elo", description = "Get matches by elo")
    @GetMapping("/matches/byElo/{minElo}/{maxElo}")
    public ResponseEntity<?> getMatchesByElo(
            @PathVariable int minElo, @PathVariable int maxElo) {
        try {
            List<Match> matches = matchService.getMatchesByElo(minElo, maxElo);
            return ResponseEntity.ok(matches);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Get matches by player", description = "Get matches by player")
    @GetMapping("/matches/{username}")
    public ResponseEntity<?> getMatchesByPlayer(
            @PathVariable String username) {
        try {
            List<Match> matches = matchService.getMatchesByPlayer(username);
            return ResponseEntity.ok(matches);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Openings with higher win rate", description = "Get openings with higher win rate")
    @GetMapping("/openings/higher-win-rate/{eloMin}/{eloMax}")
    public ResponseEntity<?> getOpeningWithHigherWinRatePerElo(@PathVariable int eloMin,
                                                               @PathVariable int eloMax) {
        try {
            List<Document> openingWithHigherWinRate = matchService.getOpeningWithHigherWinRatePerElo(eloMin, eloMax);
            return ResponseEntity.ok(openingWithHigherWinRate);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Most played openings", description = "Get most played openings")
    @GetMapping("/openings/most-played/{eloMin}/{eloMax}")
    public ResponseEntity<?> getMostPlayedOpeningsPerElo(
            @PathVariable int eloMin, @PathVariable int eloMax) {

        List<Document> mostPlayedOpenings = matchService.getMostPlayedOpeningsPerElo(eloMin, eloMax);

        if (mostPlayedOpenings == null || mostPlayedOpenings.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(mostPlayedOpenings);
    }

    @Operation(summary = "Get number of wins and draws per elo", description = "Get number of wins and draws per elo")
    @GetMapping("/matches/win-stats/{eloMin}/{eloMax}")
    public ResponseEntity<?> getNumOfWinsAndDrawsPerElo(
            @PathVariable int eloMin, @PathVariable int eloMax) {

        List<Document> result = matchService.getNumOfWinsAndDrawsPerElo(eloMin, eloMax);
        return ResponseEntity.ok(result); // Ora il tipo è Integer, non int
    }


}
