package edu.eci.dosw.oficioya.controller;

import edu.eci.dosw.oficioya.dto.SolicitudRequest;
import edu.eci.dosw.oficioya.dto.SolicitudResponse;
import edu.eci.dosw.oficioya.service.SolicitudService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/solicitudes")
public class SolicitudController {
    private final SolicitudService service;

    public SolicitudController(SolicitudService service) {
        this.service = service;
    }

    @PreAuthorize("hasRole('CONTRATANTE')")
    @PostMapping
    public ResponseEntity<SolicitudResponse> crear(@Valid @RequestBody SolicitudRequest request,
                                                   Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request, auth.getName()));
    }

    // ADMIN no accede a solicitudes: 403
    @PreAuthorize("hasAnyRole('CONTRATANTE', 'TRABAJADOR')")
    @GetMapping
    public List<SolicitudResponse> listar(Authentication auth) {
        return service.listar(auth.getName());
    }

    @PreAuthorize("hasAnyRole('CONTRATANTE', 'TRABAJADOR')")
    @GetMapping("/{id}")
    public SolicitudResponse obtener(@PathVariable Long id, Authentication auth) {
        return service.obtener(id, auth.getName());
    }

    @PreAuthorize("hasRole('TRABAJADOR')")
    @PatchMapping("/{id}/aceptar")
    public SolicitudResponse aceptar(@PathVariable Long id, Authentication auth) {
        return service.aceptar(id, auth.getName());
    }

    @PreAuthorize("hasRole('TRABAJADOR')")
    @PatchMapping("/{id}/rechazar")
    public SolicitudResponse rechazar(@PathVariable Long id, Authentication auth) {
        return service.rechazar(id, auth.getName());
    }

    @PreAuthorize("hasRole('CONTRATANTE')")
    @PatchMapping("/{id}/cancelar")
    public SolicitudResponse cancelar(@PathVariable Long id, Authentication auth) {
        return service.cancelar(id, auth.getName());
    }
}
