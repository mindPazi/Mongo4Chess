package com.example.demo.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PlayerDTO {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
}
