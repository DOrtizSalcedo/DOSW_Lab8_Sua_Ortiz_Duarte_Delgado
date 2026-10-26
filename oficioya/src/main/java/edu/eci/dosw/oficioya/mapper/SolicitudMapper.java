package edu.eci.dosw.oficioya.mapper;

import edu.eci.dosw.oficioya.dto.SolicitudResponse;
import edu.eci.dosw.oficioya.entity.Solicitud;
import org.springframework.stereotype.Component;

@Component
public class SolicitudMapper {
    public SolicitudResponse toSolicitudResponse(Solicitud solicitud) {
        if (solicitud == null) return null;

        return new SolicitudResponse(
                solicitud.getId(),
                solicitud.getContratante().getId(),
                solicitud.getContratante().getNombre(),
                solicitud.getTrabajador().getId(),
                solicitud.getTrabajador().getNombre(),
                solicitud.getDescripcion(),
                solicitud.getEstado(),
                solicitud.getFecha_creacion()
        );
    }
}
