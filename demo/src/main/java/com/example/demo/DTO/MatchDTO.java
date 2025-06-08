package com.example.demo.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;
import java.util.List;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class MatchDTO {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    @Past
    private Date date;

    @NotBlank(message = "The white player is mandatory")
    private String white;

    @NotBlank(message = "The black player is mandatory")
    private String black;

    @NotBlank(message = "The result is mandatory (e.g., 1-0, 1/2-1/2, 0-1)")
    private String result;

    @NotBlank(message = "The ECO is mandatory")
    private String eco;

    @NotNull(message = "The number of plyCount is mandatory")
    @Positive(message = "The number of plyCount must be positive")
    private Integer plyCount;

    @NotBlank(message = "The reason is mandatory (e.g., checkmate, stalemate, draw)")
    private String reason;

    private List<String> moves;

}