package com.example.demo.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class TournamentPlayerDTO {
    @NotBlank
    private String username;

    @NotNull
    @Positive
    private Integer position;
}
