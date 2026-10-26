package edu.eci.dosw.oficioya.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank @Email String correo,
                           String password) {
    public LoginRequest {
        if(correo != null) correo = correo.trim();
    }
}
