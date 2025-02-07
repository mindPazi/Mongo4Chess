package com.example.demo.service;

import com.example.demo.model.Tournament;
import org.springframework.stereotype.Service;

@Service

public class TournamentService {
    public String createTournament(Tournament tournament) {
        return "Tournament created successfully: " + tournament.toString();
    }

}
