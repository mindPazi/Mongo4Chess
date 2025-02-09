package com.example.demo.controller;

import lombok.RequiredArgsConstructor;
import com.example.demo.model.Match;

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

    @PostMapping("/match")
    public ResponseEntity<Match> createMatch(@RequestBody Match match) {
        matchService.saveMatch(match);
        return ResponseEntity.ok(match);
    }

    // implementare i metodi per admin e player capendo come gestire
    // l'autenticazione
}
