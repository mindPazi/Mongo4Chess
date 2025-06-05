package com.example.demo.model;

import lombok.Data;

// the entire match and rank are saved,
// so that the seeMostImportantMatchForTournament query can be executed
@Data
public class TournamentMatch {
    private String matchGrade;
    private Match match;
}
