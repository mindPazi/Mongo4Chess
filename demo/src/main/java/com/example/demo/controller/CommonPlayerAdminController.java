package com.example.demo.controller;

import com.example.demo.DTO.MatchDTO;
import com.example.demo.DTO.TournamentDTO;
import com.example.demo.DTO.TournamentMatchDTO;
import com.example.demo.DTO.TournamentPlayerDTO;
import com.example.demo.model.Match;
import com.example.demo.model.Tournament;
import com.example.demo.model.TournamentMatch;
import com.example.demo.model.TournamentPlayer;
import com.example.demo.service.AdminService;
import com.example.demo.service.MatchService;
import com.example.demo.service.PlayerService;
import com.example.demo.service.TournamentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
public abstract class CommonPlayerAdminController {
    protected final PlayerService playerService;
    protected final MatchService matchService;
    protected final AdminService adminService;
    protected final TournamentService tournamentService;

    // update the final positions, including the winner field
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
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    public ResponseEntity<?> saveMatch(@RequestBody @Valid MatchDTO matchDTO) {
        try {
            Match match = new Match();
            BeanUtils.copyProperties(matchDTO, match);
            matchService.saveMatch(match);
            return ResponseEntity.status(HttpStatus.CREATED).body(match); // Return the created Match object
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    public ResponseEntity<?> createTournament(@RequestBody @Valid TournamentDTO tournamentDTO) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();

            // Copy the DTO fields in a tournament object
            Tournament tournament = new Tournament();
            BeanUtils.copyProperties(tournamentDTO, tournament);
            tournament.setCreator(currentUsername);
            tournament.setIsClosed(false);

            Tournament createdTournament = tournamentService.createTournament(tournament);

            // Create the tournament
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTournament);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

    public ResponseEntity<?> getAllTournaments() {
        try {
            return ResponseEntity.ok(tournamentService.getAllTournaments());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    public ResponseEntity<?> deleteTournament(@PathVariable String tournamentId) {
        if (tournamentId == null || tournamentId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tournament ID not found.");
        }
        try {
            tournamentService.deleteTournament(tournamentId);
            return ResponseEntity.ok("Tournament deleted successfuly.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

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
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    public ResponseEntity<?> getTournamentsByDate(@PathVariable @Valid @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate, @PathVariable @Valid @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {
        try {
            return ResponseEntity.ok(tournamentService.getTournamentsByDate(startDate, endDate));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    public ResponseEntity<?> getTournamentsByCreator(@PathVariable String creator) {
        try {
            return ResponseEntity.ok(tournamentService.getCreatedTournaments(creator));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}