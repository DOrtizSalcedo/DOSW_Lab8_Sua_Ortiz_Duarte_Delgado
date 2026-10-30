package edu.eci.dosw.oficioya.controller;

import edu.eci.dosw.oficioya.dto.LoginRequest;
import edu.eci.dosw.oficioya.dto.LoginResponse;
import edu.eci.dosw.oficioya.dto.RegistroRequest;
import edu.eci.dosw.oficioya.dto.UsuarioResponse;
import edu.eci.dosw.oficioya.service.UsuarioService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final UsuarioService service;

    public AuthController(UsuarioService service) {
        this.service = service;
    }

    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistroRequest registroUsuario) {
        UsuarioResponse respuestaHTTP = service.registrar(registroUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuestaHTTP);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest solicitud) {
        LoginResponse respuestaLogin = service.login(solicitud);
        return ResponseEntity.ok(respuestaLogin);
    }
}
