package edu.eci.dosw.oficioya.service;

import edu.eci.dosw.oficioya.dto.PerfilPropio;
import edu.eci.dosw.oficioya.entity.Usuario;
import edu.eci.dosw.oficioya.exception.RecursoNoEncontradoException;
import edu.eci.dosw.oficioya.mapper.PerfilMapper;
import edu.eci.dosw.oficioya.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import edu.eci.dosw.oficioya.dto.PerfilPublico;
import edu.eci.dosw.oficioya.entity.Rol;
import java.util.List;

@Service
public class PerfilService {

    private final UsuarioRepository usuarioRepository;
    private final PerfilMapper mapper;

    public PerfilService(UsuarioRepository usuarioRepository, PerfilMapper mapper) {
        this.usuarioRepository = usuarioRepository;
        this.mapper = mapper;
    }

    public PerfilPropio miPerfil(String correo) {
        Usuario u = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        return mapper.toPropio(u);
    }

    public List<PerfilPublico> listarTrabajadores() {
        return usuarioRepository.findByRol(Rol.TRABAJADOR).stream()
                .map(mapper::toPublico)
                .toList();
    }

    public PerfilPublico obtenerTrabajador(Long id) {
        return usuarioRepository.findByIdAndRol(id, Rol.TRABAJADOR)
                .map(mapper::toPublico)
                .orElseThrow(() -> new RecursoNoEncontradoException("Trabajador no encontrado"));
    }
}