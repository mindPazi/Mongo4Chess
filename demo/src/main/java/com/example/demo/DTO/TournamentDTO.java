package com.example.demo.DTO;

import com.example.demo.model.Player;
import com.example.demo.validation.PowerOfTwo;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;
import java.util.List;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class TournamentDTO {
    @NotBlank
    private String name;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date startDate;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date endDate;

    @NotNull
    @Min(2)
    @Max(64)
    @PowerOfTwo
    private int maxPlayers;

    @NotBlank
    private String description;

    @NotNull
    @Min(0)
    private int eloMin;

    @NotNull
    @Min(0)
    private int eloMax;

}
