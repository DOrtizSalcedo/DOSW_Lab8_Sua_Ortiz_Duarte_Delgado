package edu.eci.dosw.oficioya;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.eci.dosw.oficioya.entity.Rol;
import edu.eci.dosw.oficioya.entity.Usuario;
import edu.eci.dosw.oficioya.repository.UsuarioRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PerfilesSeguridadTest {

    @Autowired MockMvc mockMvc;
    @Autowired UsuarioRepository usuarioRepository;

    private Long idTrabajador;

    @BeforeEach
    void preparar() {
        usuarioRepository.deleteAll();

        Usuario trabajador = new Usuario();
        trabajador.setNombre("Pedro Gómez");
        trabajador.setCorreo("pedro@example.com");
        trabajador.setTelefono("3001234567");
        trabajador.setPassword_hash("hash-de-prueba");
        trabajador.setRol(Rol.TRABAJADOR);
        trabajador.setOficio_principal("Plomero");
        trabajador.setZona("Chapinero");
        trabajador.setFecha_creacion(LocalDate.now());
        idTrabajador = usuarioRepository.save(trabajador).getId();
    }

    @Test
    @WithMockUser(username = "ana@example.com", roles = "CONTRATANTE")
    @DisplayName("PT-10: el perfil público no expone correo ni teléfono")
    void perfilPublicoSinContacto() throws Exception {
        mockMvc.perform(get("/api/v1/trabajadores/" + idTrabajador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Pedro Gómez"))
                .andExpect(jsonPath("$.correo").doesNotExist())
                .andExpect(jsonPath("$.telefono").doesNotExist());
    }

    @Test
    @WithMockUser(username = "ana@example.com", roles = "CONTRATANTE")
    @DisplayName("PT-11: un CONTRATANTE no puede consultar /admin/usuarios")
    void contratanteNoAccedeAAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/usuarios"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    @DisplayName("PT-12: un ADMIN consulta /admin/usuarios y ve los datos de contacto")
    void adminVeDatosDeContacto() throws Exception {
        mockMvc.perform(get("/api/v1/admin/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].correo").value("pedro@example.com"))
                .andExpect(jsonPath("$[0].telefono").value("3001234567"));
    }
}