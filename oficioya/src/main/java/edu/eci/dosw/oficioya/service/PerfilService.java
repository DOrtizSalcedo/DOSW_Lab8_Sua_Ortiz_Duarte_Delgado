package edu.eci.dosw.oficioya.service;

import edu.eci.dosw.oficioya.dto.PerfilPropio;
import edu.eci.dosw.oficioya.entity.Usuario;
import edu.eci.dosw.oficioya.exception.RecursoNoEncontradoException;
import edu.eci.dosw.oficioya.mapper.PerfilMapper;
import edu.eci.dosw.oficioya.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
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
        List<Usuario> encontrados = usuarioRepository.findByCorreo(correo);
        if (encontrados.isEmpty()) {
            throw new RecursoNoEncontradoException("Usuario no encontrado");
        }
        return mapper.toPropio(encontrados.get(0));
    }
}