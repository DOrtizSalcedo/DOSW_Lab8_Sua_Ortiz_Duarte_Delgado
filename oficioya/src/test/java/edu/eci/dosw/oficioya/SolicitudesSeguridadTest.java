package edu.eci.dosw.oficioya;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.eci.dosw.oficioya.entity.EstadoSolicitud;
import edu.eci.dosw.oficioya.entity.Rol;
import edu.eci.dosw.oficioya.entity.Solicitud;
import edu.eci.dosw.oficioya.entity.Usuario;
import edu.eci.dosw.oficioya.repository.SolicitudRepository;
import edu.eci.dosw.oficioya.repository.UsuarioRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SolicitudesSeguridadTest {

    private static final String URL = "/api/v1/solicitudes";

    @Autowired MockMvc mockMvc;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired SolicitudRepository solicitudRepository;

    private Long idAna;
    private Long idPedro;
    private Long idSolicitudDeAna;

    @BeforeEach
    void preparar() {
        // Las solicitudes van primero: referencian a los usuarios
        solicitudRepository.deleteAll();
        usuarioRepository.deleteAll();

        Usuario ana = usuarioRepository.save(usuario("Ana Ruiz", "ana@example.com", Rol.CONTRATANTE));
        Usuario luis = usuarioRepository.save(usuario("Luis Mora", "luis@example.com", Rol.CONTRATANTE));
        Usuario pedro = usuarioRepository.save(usuario("Pedro Gómez", "pedro@example.com", Rol.TRABAJADOR));
        usuarioRepository.save(usuario("María López", "maria@example.com", Rol.TRABAJADOR));
        idAna = ana.getId();
        idPedro = pedro.getId();

        idSolicitudDeAna = solicitudRepository.save(new Solicitud(ana, pedro,
                "Tengo una gotera en el baño", EstadoSolicitud.PENDIENTE, LocalDate.now())).getId();
        solicitudRepository.save(new Solicitud(luis, pedro,
                "Cambiar la llave del lavamanos", EstadoSolicitud.PENDIENTE, LocalDate.now()));
    }

    // Las otras clases de prueba borran usuarios y fallarían si quedan solicitudes
    @AfterEach
    void limpiar() {
        solicitudRepository.deleteAll();
    }

    private Usuario usuario(String nombre, String correo, Rol rol) {
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setCorreo(correo);
        usuario.setTelefono("3001234567");
        usuario.setPassword_hash("hash-de-prueba");
        usuario.setRol(rol);
        usuario.setFecha_creacion(LocalDate.now());
        return usuario;
    }

    private EstadoSolicitud estadoDeLaSolicitudDeAna() {
        return solicitudRepository.findById(idSolicitudDeAna).orElseThrow().getEstado();
    }

    @Test
    @WithMockUser(username = "pedro@example.com", roles = "TRABAJADOR")
    @DisplayName("PT-13: un TRABAJADOR no puede crear solicitudes")
    void trabajadorNoCreaSolicitudes() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trabajadorId\": " + idPedro + ", \"descripcion\": \"Arreglar la ducha\"}"))
                .andExpect(status().isForbidden());

        assertEquals(2, solicitudRepository.count());
    }

    @Test
    @WithMockUser(username = "ana@example.com", roles = "CONTRATANTE")
    @DisplayName("PT-14: un contratante solo ve sus propias solicitudes")
    void contratanteListaSoloLasSuyas() throws Exception {
        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(idSolicitudDeAna))
                .andExpect(jsonPath("$[0].contratanteId").value(idAna));
    }

    @Test
    @WithMockUser(username = "luis@example.com", roles = "CONTRATANTE")
    @DisplayName("PT-15: consultar una solicitud ajena responde 404")
    void consultarSolicitudAjena() throws Exception {
        mockMvc.perform(get(URL + "/" + idSolicitudDeAna))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "luis@example.com", roles = "CONTRATANTE")
    @DisplayName("PT-15: cancelar una solicitud ajena responde 404 sin cambios en la base de datos")
    void cancelarSolicitudAjena() throws Exception {
        mockMvc.perform(patch(URL + "/" + idSolicitudDeAna + "/cancelar"))
                .andExpect(status().isNotFound());

        assertEquals(EstadoSolicitud.PENDIENTE, estadoDeLaSolicitudDeAna());
    }

    @Test
    @WithMockUser(username = "maria@example.com", roles = "TRABAJADOR")
    @DisplayName("PT-16: aceptar con un trabajador no asignado responde 404")
    void trabajadorNoAsignadoNoAcepta() throws Exception {
        mockMvc.perform(patch(URL + "/" + idSolicitudDeAna + "/aceptar"))
                .andExpect(status().isNotFound());

        assertEquals(EstadoSolicitud.PENDIENTE, estadoDeLaSolicitudDeAna());
    }

    @Test
    @WithMockUser(username = "ana@example.com", roles = "CONTRATANTE")
    @DisplayName("PT-16: un contratante no puede aceptar una solicitud: 403")
    void contratanteNoAcepta() throws Exception {
        mockMvc.perform(patch(URL + "/" + idSolicitudDeAna + "/aceptar"))
                .andExpect(status().isForbidden());

        assertEquals(EstadoSolicitud.PENDIENTE, estadoDeLaSolicitudDeAna());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    @DisplayName("PT-17: un ADMIN no puede consultar /solicitudes")
    void adminNoAccedeASolicitudes() throws Exception {
        mockMvc.perform(get(URL))
                .andExpect(status().isForbidden());
    }
}
