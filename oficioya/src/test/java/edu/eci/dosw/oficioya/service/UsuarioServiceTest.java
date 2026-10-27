package edu.eci.dosw.oficioya.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import edu.eci.dosw.oficioya.dto.LoginRequest;
import edu.eci.dosw.oficioya.dto.LoginResponse;
import edu.eci.dosw.oficioya.entity.Rol;
import edu.eci.dosw.oficioya.entity.Usuario;
import edu.eci.dosw.oficioya.exception.CredencialesInvalidasException;
import edu.eci.dosw.oficioya.mapper.UsuarioMapper;
import edu.eci.dosw.oficioya.repository.UsuarioRepository;
import edu.eci.dosw.oficioya.security.JwtService;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {
    @Mock UsuarioRepository repository;
    @Mock PasswordEncoder encoder;
    @Mock UsuarioMapper mapper;
    @Mock JwtService jwtService;

    private UsuarioService service;
    private Usuario matias;

    @BeforeEach
    void setUp() {
        service = new UsuarioService(repository, encoder, mapper, jwtService, 30);
        matias = new Usuario("Matias", "matias@gmail.com", "3001234567", "HASH",
                Rol.TRABAJADOR, "Plomero", "Chapinero", LocalDate.now());
    }

    @Test
    void loginCorrecto() {
        when(repository.findByCorreo("matias@gmail.com")).thenReturn(Optional.of(matias));
        when(encoder.matches("clave1234", "HASH")).thenReturn(true);
        when(jwtService.generar("matias@gmail.com", "TRABAJADOR")).thenReturn("TOKEN");

        LoginResponse r = service.login(new LoginRequest("matias@gmail.com", "clave1234"));

        assertEquals("Bearer", r.tipoToken());
        assertEquals("TOKEN", r.accesoToken());
        assertEquals(1800, r.expiraEn());
    }

    @Test
    void normalizaCorreo() {
        when(repository.findByCorreo("matias@gmail.com")).thenReturn(Optional.of(matias));
        when(encoder.matches("clave1234", "HASH")).thenReturn(true);
        when(jwtService.generar(anyString(), anyString())).thenReturn("TOKEN");

        service.login(new LoginRequest("  MATIAS@Gmail.com ", "clave1234"));

        verify(repository).findByCorreo("matias@gmail.com");
    }

    @Test
    void contraErronea() {
        when(repository.findByCorreo("matias@gmail.com")).thenReturn(Optional.of(matias));
        when(encoder.matches("mala", "HASH")).thenReturn(false);

        assertThrows(CredencialesInvalidasException.class,
                () -> service.login(new LoginRequest("matias@gmail.com", "mala")));
        verifyNoInteractions(jwtService);
    }

    @Test
    void correoInexistente() {
        when(repository.findByCorreo("hola@gmail.com")).thenReturn(Optional.empty());

        CredencialesInvalidasException ex = assertThrows(CredencialesInvalidasException.class,
                () -> service.login(new LoginRequest("hola@gmail.com", "clave1234")));

        assertEquals("Credenciales invalidas", ex.getMessage());
        verify(encoder, never()).matches(anyString(), anyString());
        verifyNoInteractions(jwtService);
    }
}