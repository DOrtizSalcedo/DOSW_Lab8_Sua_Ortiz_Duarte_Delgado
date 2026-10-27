package edu.eci.dosw.oficioya.controller;

import edu.eci.dosw.oficioya.dto.PerfilAdmin;
import edu.eci.dosw.oficioya.service.PerfilService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final PerfilService perfilService;

    public AdminController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/usuarios")
    public List<PerfilAdmin> listar() {
        return perfilService.listarParaAdmin();
    }
}
