package edu.eci.dosw.oficioya.dto;

import edu.eci.dosw.oficioya.entity.EstadoSolicitud;

import java.time.LocalDate;

// Sin correo ni teléfono de los participantes
public record SolicitudResponse(Long id,
                                Long contratanteId,
                                String contratanteNombre,
                                Long trabajadorId,
                                String trabajadorNombre,
                                String descripcion,
                                EstadoSolicitud estado,
                                LocalDate fechaCreacion
) {
}
