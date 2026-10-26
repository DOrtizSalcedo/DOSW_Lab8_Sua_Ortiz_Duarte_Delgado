package edu.eci.dosw.oficioya.service;

import edu.eci.dosw.oficioya.dto.SolicitudRequest;
import edu.eci.dosw.oficioya.dto.SolicitudResponse;
import edu.eci.dosw.oficioya.entity.EstadoSolicitud;
import edu.eci.dosw.oficioya.entity.Rol;
import edu.eci.dosw.oficioya.entity.Solicitud;
import edu.eci.dosw.oficioya.entity.Usuario;
import edu.eci.dosw.oficioya.exception.EstadoInvalidoException;
import edu.eci.dosw.oficioya.exception.RecursoNoEncontradoException;
import edu.eci.dosw.oficioya.mapper.SolicitudMapper;
import edu.eci.dosw.oficioya.repository.SolicitudRepository;
import edu.eci.dosw.oficioya.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class SolicitudService {
    private static final String NO_ENCONTRADA = "Solicitud no encontrada";

    private final SolicitudRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final SolicitudMapper mapper;

    public SolicitudService(SolicitudRepository repository, UsuarioRepository usuarioRepository,
                            SolicitudMapper mapper) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.mapper = mapper;
    }

    @Transactional
    public SolicitudResponse crear(SolicitudRequest request, String correoAutenticado) {
        Usuario contratante = usuarioActual(correoAutenticado);

        // Error 404: el trabajador no existe o no es trabajdor
        Usuario trabajador = usuarioRepository.findById(request.trabajadorId())
                .filter(u -> u.getRol() == Rol.TRABAJADOR)
                .orElseThrow(() -> new RecursoNoEncontradoException("Trabajador no encontrado"));

        Solicitud solicitud = new Solicitud(contratante, trabajador, request.descripcion(),
                EstadoSolicitud.PENDIENTE, LocalDate.now());
        return mapper.toSolicitudResponse(repository.save(solicitud));
    }

    @Transactional(readOnly = true)
    public List<SolicitudResponse> listar(String correoAutenticado) {
        Usuario actual = usuarioActual(correoAutenticado);
        List<Solicitud> solicitudes = actual.getRol() == Rol.CONTRATANTE
                ? repository.findByContratanteId(actual.getId())
                : repository.findByTrabajadorId(actual.getId());
        return solicitudes.stream().map(mapper::toSolicitudResponse).toList();
    }

    @Transactional(readOnly = true)
    public SolicitudResponse obtener(Long id, String correoAutenticado) {
        Usuario actual = usuarioActual(correoAutenticado);
        return repository.findByIdAndParticipante(id, actual.getId())
                .map(mapper::toSolicitudResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException(NO_ENCONTRADA));
    }

    @Transactional
    public SolicitudResponse aceptar(Long id, String correoAutenticado) {
        return responder(id, correoAutenticado, EstadoSolicitud.ACEPTADA);
    }

    @Transactional
    public SolicitudResponse rechazar(Long id, String correoAutenticado) {
        return responder(id, correoAutenticado, EstadoSolicitud.RECHAZADA);
    }

    @Transactional
    public SolicitudResponse cancelar(Long id, String correoAutenticado) {
        Usuario actual = usuarioActual(correoAutenticado);
        Solicitud solicitud = repository.findByIdAndContratanteId(id, actual.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException(NO_ENCONTRADA));

        // Error 409: solo se cancela una solicitud pendiente o aceptada
        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE
                && solicitud.getEstado() != EstadoSolicitud.ACEPTADA) {
            throw new EstadoInvalidoException("Solo se puede cancelar una solicitud pendiente o aceptada");
        }
        solicitud.setEstado(EstadoSolicitud.CANCELADA);
        return mapper.toSolicitudResponse(repository.save(solicitud));
    }

    private SolicitudResponse responder(Long id, String correoAutenticado, EstadoSolicitud nuevoEstado) {
        Usuario actual = usuarioActual(correoAutenticado);
        // Error 404: solo el trabajador asignado puede responder
        Solicitud solicitud = repository.findByIdAndTrabajadorId(id, actual.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException(NO_ENCONTRADA));

        // Error 409: solo se responde una solicitud pendiente
        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new EstadoInvalidoException("Solo se puede responder una solicitud pendiente");
        }
        solicitud.setEstado(nuevoEstado);
        return mapper.toSolicitudResponse(repository.save(solicitud));
    }

    // La identidad sale siempre del contexto de seguridad, nunca del cuerpo ni de la URL
    private Usuario usuarioActual(String correoAutenticado) {
        return usuarioRepository.findByCorreo(correoAutenticado)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }
}
