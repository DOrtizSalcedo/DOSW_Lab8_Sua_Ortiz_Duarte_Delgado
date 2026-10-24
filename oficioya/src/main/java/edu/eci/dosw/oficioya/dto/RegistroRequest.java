package edu.eci.dosw.oficioya.dto;

import edu.eci.dosw.oficioya.entity.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RegistroRequest(@NotBlank String nombre,
                              @NotBlank @Email String correo,
                              @NotBlank String telefono,
                              @NotBlank @Size(min = 8) String password_hash,
                              @NotBlank Rol rol,
                              @NotBlank String oficio_principal,
                              @NotBlank String zona,
                              @NotBlank LocalDate fecha_creacion
) {
}
