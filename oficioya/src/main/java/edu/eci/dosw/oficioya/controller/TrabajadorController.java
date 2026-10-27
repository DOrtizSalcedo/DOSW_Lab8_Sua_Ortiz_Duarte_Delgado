package edu.eci.dosw.oficioya.controller;

import edu.eci.dosw.oficioya.dto.PerfilPublico;
import edu.eci.dosw.oficioya.service.PerfilService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trabajadores")
public class TrabajadorController {

    private final PerfilService perfilService;

    public TrabajadorController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    @GetMapping
    public List<PerfilPublico> listar() {
        return perfilService.listarTrabajadores();
    }

    @GetMapping("/{id}")
    public PerfilPublico obtener(@PathVariable Long id) {
        return perfilService.obtenerTrabajador(id);
    }
}