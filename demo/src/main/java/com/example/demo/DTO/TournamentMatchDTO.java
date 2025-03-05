package com.example.demo.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TournamentMatchDTO {
    @NotBlank
    private MatchDTO match;
    @NotBlank(message = "Es: final, semifinal, quarterfinal")
    private String matchGrade;
}
