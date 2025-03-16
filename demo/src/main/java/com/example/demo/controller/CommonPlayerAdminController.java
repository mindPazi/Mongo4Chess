package com.example.demo.controller;

import com.example.demo.DTO.MatchDTO;
import com.example.demo.DTO.TournamentDTO;
import com.example.demo.DTO.TournamentPlayerDTO;
import com.example.demo.model.Match;
import com.example.demo.model.Tournament;
import com.example.demo.model.TournamentPlayer;
import com.example.demo.service.AdminService;
import com.example.demo.service.MatchService;
import com.example.demo.service.PlayerService;
import com.example.demo.service.TournamentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public abstract class CommonPlayerAdminController {
    protected final PlayerService playerService;
    protected final MatchService matchService;
    protected final AdminService adminService;
    protected final TournamentService tournamentService;

    //aggiorna le posizioni finali dei giocatori al torneo, aggiornando anche il campo winner
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

    public ResponseEntity<?> saveMatch(@RequestBody @Valid MatchDTO matchDTO) {
        try {
            Match match = new Match();
            BeanUtils.copyProperties(matchDTO, match);
            matchService.saveMatch(match);
            return ResponseEntity.status(HttpStatus.CREATED).body(match); // Restituisci l'oggetto Match creato
        } catch (Exception e) {
            // Gestione degli errori
            System.out.println(e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

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

            Tournament createdTournament = tournamentService.createTournament(tournament);

            // Crea il torneo
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTournament);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore durante la creazione del torneo: " + e.getMessage());
        }
    }

    public ResponseEntity<?> getAllTournaments() {
        try {
            return ResponseEntity.ok(tournamentService.getAllTournaments());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore durante il recupero dei tornei: " + e.getMessage());
        }
    }

}