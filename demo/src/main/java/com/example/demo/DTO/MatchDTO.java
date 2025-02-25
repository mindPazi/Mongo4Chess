package com.example.demo.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;
import java.util.List;

import jakarta.validation.constraints.*;
import lombok.Data;

//todo: assicurarsi che l'Elo venga aggiunto nel db dal sistema e non con il post del match
@Data
public class MatchDTO {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    @Past
    private Date date;

    @NotBlank(message = "Il giocatore bianco è obbligatorio")
    private String white;

    @NotBlank(message = "Il giocatore nero è obbligatorio")
    private String black;

    @NotBlank(message = "Il risultato è obbligatorio (es:1-0, 1/2-1/2, 0-1)")
    private String result;

    @NotBlank(message = "Il time control è obbligatorio (es: 10, 120)")
    private String timeControl;

    @NotBlank(message = "L'ECO è obbligatorio")
    private String ECO;

    @NotNull(message = "Il numero di plyCount è obbligatorio")
    @Positive(message = "Il numero di plyCount deve essere positivo")
    private Integer plyCount;

    @NotBlank(message = "La reason è obbligatoria (es: checkmate, stalemate, draw)")
    private String reason;

    @Null
    private List<String> moves;

}