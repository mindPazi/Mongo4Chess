package com.example.demo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.example.demo.model.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.demo.service.PlayerService;
import com.example.demo.DTO.PlayerDTO;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/register")
public class RegistrationController {
    @Autowired
    private final PlayerService playerService;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    @PostMapping
    public ResponseEntity<String> registerPlayer(@RequestBody @Valid PlayerDTO playerDTO) {
        String encodedPassword = passwordEncoder.encode(playerDTO.getPassword());
        // a new player is created with elo 0
        playerService.setPlayer(new Player(playerDTO.getUsername(), encodedPassword));
        //playerService.setPlayer(new Player(username, password, 0));
        return ResponseEntity.ok(playerService.createPlayer(playerDTO.getUsername(), encodedPassword, 0));
    }
}
