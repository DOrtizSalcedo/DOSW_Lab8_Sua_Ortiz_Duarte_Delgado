package edu.eci.dosw.oficioya.service;

import java.util.EnumSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import edu.eci.dosw.oficioya.dto.LoginRequest;
import edu.eci.dosw.oficioya.dto.LoginResponse;
import edu.eci.dosw.oficioya.dto.RegistroRequest;
import edu.eci.dosw.oficioya.dto.UsuarioResponse;
import edu.eci.dosw.oficioya.entity.Rol;
import edu.eci.dosw.oficioya.entity.Usuario;
import edu.eci.dosw.oficioya.exception.CorreoDuplicadoException;
import edu.eci.dosw.oficioya.exception.CredencialesInvalidasException;
import edu.eci.dosw.oficioya.exception.RolNoPermitidoException;
import edu.eci.dosw.oficioya.mapper.UsuarioMapper;
import edu.eci.dosw.oficioya.repository.UsuarioRepository;
import edu.eci.dosw.oficioya.security.JwtService;

@Service
public class UsuarioService {
    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;
    private final UsuarioMapper mapper;
    private final JwtService jwtService;
    @Value("${app.jwt.ttl-minutes:30}")
    private long ttlMinutes;

    private static final Set<Rol> ROLES_PUBLICOS = EnumSet.of(Rol.TRABAJADOR,
            Rol.CONTRATANTE);

    public UsuarioService(UsuarioRepository repository, PasswordEncoder encoder, UsuarioMapper mapper,
                          JwtService jwtService, @Value("${app.jwt.ttl-minutes}") long ttlMinutes) {
        this.repository = repository;
        this.encoder = encoder;
        this.mapper = mapper;
        this.jwtService = jwtService;
        this.ttlMinutes = ttlMinutes;
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

    public LoginResponse login(LoginRequest solicitud) {
        Usuario usuario = repository.findByCorreo(solicitud.correo().trim().toLowerCase())
                .filter(x -> encoder.matches(solicitud.password(), x.getPassword_hash()))
                .orElseThrow(CredencialesInvalidasException::new);

        String token = jwtService.generar(usuario.getCorreo(), usuario.getRol().name());

        return new LoginResponse("Bearer", token, ttlMinutes * 60);
    }
}
