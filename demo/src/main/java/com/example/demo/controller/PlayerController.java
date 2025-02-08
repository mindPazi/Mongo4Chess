package com.example.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/player")
@Tag(name = "Player Controller", description = "Player operations")
public class PlayerController {

    // implementare i metodi per admin e player capendo come gestire
    // l'autenticazione
}
