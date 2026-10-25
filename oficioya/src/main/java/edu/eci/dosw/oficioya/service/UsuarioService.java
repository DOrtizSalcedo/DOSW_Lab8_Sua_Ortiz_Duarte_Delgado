package edu.eci.dosw.oficioya.service;

import edu.eci.dosw.oficioya.dto.RegistroRequest;
import edu.eci.dosw.oficioya.dto.UsuarioResponse;
import edu.eci.dosw.oficioya.entity.Rol;
import edu.eci.dosw.oficioya.entity.Usuario;
import edu.eci.dosw.oficioya.exception.CorreoDuplicadoException;
import edu.eci.dosw.oficioya.exception.RolNoPermitidoException;
import edu.eci.dosw.oficioya.mapper.UsuarioMapper;
import edu.eci.dosw.oficioya.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

@Service
public class UsuarioService {
    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;
    private final UsuarioMapper mapper;

    private static final Set<Rol> ROLES_PUBLICOS = EnumSet.of(Rol.TRABAJADOR,
            Rol.CONTRATANTE, Rol.ADMIN);

    public UsuarioService(UsuarioRepository repository, PasswordEncoder encoder, UsuarioMapper mapper) {
        this.repository = repository;
        this.encoder = encoder;
        this.mapper = mapper;
    }

    public UsuarioResponse registrar(RegistroRequest registro) {
        // Error 400
        if(!ROLES_PUBLICOS.contains(registro.rol())) {
            throw new RolNoPermitidoException("Rol no permitido al registrar");
        }

        String correo = registro.correo().trim().toLowerCase();

        // Error 409
        if(repository.existsByCorreo(correo)) {
            throw new CorreoDuplicadoException(correo);
        }

        Usuario usuario = new Usuario(registro.nombre(),
                correo,
                registro.telefono(),
                encoder.encode(registro.password_hash()),
                registro.rol(),
                registro.oficio_principal(),
                registro.zona(),
                registro.fecha_creacion()
        );
        return mapper.toUsuarioResponse(repository.save(usuario));
    }
}
