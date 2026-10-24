package edu.eci.dosw.oficioya.dto;

import edu.eci.dosw.oficioya.entity.Rol;

import java.time.LocalDate;

public record UsuarioResponse (Long id,
                               String nombre,
                               String correo,
                               String telefono,
                               Rol rol,
                               String oficio_principal,
                               String zona,
                               LocalDate fecha_creacion
){
}
