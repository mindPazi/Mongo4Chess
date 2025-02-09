package com.example.demo.controller;

import lombok.RequiredArgsConstructor;
import com.example.demo.model.Match;
import com.example.demo.model.Player;
import com.example.demo.model.PlayerNode;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.example.demo.service.PlayerService;
import com.example.demo.service.TournamentService;
import com.example.demo.model.Tournament;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/player")
@Tag(name = "Player Controller", description = "Player operations")
public class PlayerController {

    @AutoWired
    private PlayerService playerService;

    @AutoWired
    private TournamentService tournamentService;

    @AutoWired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/match")
    public ResponseEntity<Match> createMatch(@RequestBody Match match) {
        return ResponseEntity.ok(playerService.createMatch(match));
    }



    // implementare i metodi per admin e player capendo come gestire
    // l'autenticazione
}
