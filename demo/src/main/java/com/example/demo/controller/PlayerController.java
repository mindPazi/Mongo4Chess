package com.example.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.demo.service.MatchService;
import com.example.demo.service.PlayerService;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/player")
@Tag(name = "Player Controller", description = "Player operations")
public class PlayerController {

    private final MatchService matchService;
    private final PlayerService playerService;

    // implementare i metodi per admin e player capendo come gestire
    // l'autenticazione
}
