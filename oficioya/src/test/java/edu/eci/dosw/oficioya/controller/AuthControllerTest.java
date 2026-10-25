package edu.eci.dosw.oficioya.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import edu.eci.dosw.oficioya.service.JwtService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import edu.eci.dosw.oficioya.entity.Usuario;
import edu.eci.dosw.oficioya.repository.UsuarioRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    private static final String REGISTRO_URL = "/api/v1/auth/registro";
    private static final String LOGIN_URL = "/api/v1/auth/login";

    private static final String CORREO = "miguel@hotmail.com";
    private static final String PASSWORD = "ContrasenaDemo123!";

    @Autowired MockMvc mockMvc;
    @Autowired UsuarioRepository repository;
    @Autowired PasswordEncoder encoder;
    @Autowired
    JwtService jwtService;

    @BeforeEach
    void limpiar() {
        repository.deleteAll();
    }

    private String json(String correo, String password, String rol) {
        return """
                {
                  "nombre": "Miguel Lizarazo",
                  "correo": "%s",
                  "telefono": "3001234567",
                  "password_hash": "%s",
                  "rol": "%s",
                  "oficio_principal": "Plomero",
                  "zona": "Chapinero",
                  "fecha_creacion": "2026-10-25"
                }
                """.formatted(correo, password, rol);
    }

    private ResultActions registrar(String json) throws Exception {
        return mockMvc.perform(post(REGISTRO_URL).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private void crearTrabajador() throws Exception {
        registrar(json(CORREO, PASSWORD, "TRABAJADOR")).andExpect(status().isCreated());
    }

    // PT1

    @Test
    void registroValido() throws Exception {
        registrar(json(CORREO, PASSWORD, "TRABAJADOR"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.rol").value("TRABAJADOR"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.password_hash").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void registroContratante() throws Exception {
        registrar(json("ana@outlook.com", PASSWORD, "CONTRATANTE"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("CONTRATANTE"));
    }

    @Test
    void correoNormalizado() throws Exception {
        registrar(json("  MIGUEL@Hotmail.COM ", PASSWORD, "TRABAJADOR")).andExpect(status().isCreated());

        assertTrue(repository.findByCorreo(CORREO).isPresent());
    }

    // PT2

    @Test
    void correoRepetido() throws Exception {
        registrar(json(CORREO, PASSWORD, "TRABAJADOR")).andExpect(status().isCreated());

        registrar(json(CORREO, PASSWORD, "TRABAJADOR")).andExpect(status().isConflict());
        assertEquals(1, repository.count());
    }

    @Test
    void correoRepetidoConMayusculas() throws Exception {
        registrar(json(CORREO, PASSWORD, "TRABAJADOR")).andExpect(status().isCreated());

        registrar(json("MIGUEL@HOTMAIL.COM", PASSWORD, "CONTRATANTE")).andExpect(status().isConflict());
    }

    // PT3

    @Test
    void rolAdminNoPermitido() throws Exception {
        registrar(json(CORREO, PASSWORD, "ADMIN")).andExpect(status().isBadRequest());

        assertEquals(0, repository.count());
    }

    @Test
    void passwordCorta() throws Exception {
        registrar(json(CORREO, "xd12", "TRABAJADOR")).andExpect(status().isBadRequest());

        assertEquals(0, repository.count());
    }

    @Test
    void rolInexistente() throws Exception {
        registrar(json(CORREO, PASSWORD, "SUPERHEROE")).andExpect(status().isBadRequest());
    }

    @Test
    void faltanDatos() throws Exception {
        registrar("{\"correo\":\"" + CORREO + "\"}").andExpect(status().isBadRequest());
    }

    // PT4

    @Test
    void passwordSeGuardaConHash() throws Exception {
        registrar(json(CORREO, PASSWORD, "TRABAJADOR")).andExpect(status().isCreated());

        Usuario guardado = repository.findByCorreo(CORREO).orElseThrow();
        assertNotEquals(PASSWORD, guardado.getPassword_hash());
        assertTrue(guardado.getPassword_hash().startsWith("$2"));
        assertTrue(encoder.matches(PASSWORD, guardado.getPassword_hash()));
    }

    private String body(String correo, String password) {
        return "{\"correo\":\"" + correo + "\",\"password\":\"" + password + "\"}";
    }

    private String login(String json) throws Exception {
        return mockMvc.perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(json))
                .andReturn().getResponse().getContentAsString();
    }

    // PT5

    @Test
    void loginCorrecto() throws Exception {
        crearTrabajador();
        mockMvc.perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(body(CORREO, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoToken").value("Bearer"))
                .andExpect(jsonPath("$.accesoToken").isNotEmpty())
                .andExpect(jsonPath("$.expiraEn").value(1800));
    }

    @Test
    void tokenEmitidoEsValido() throws Exception {
        crearTrabajador();
        String token = JsonPath.read(login(body(CORREO, PASSWORD)), "$.accesoToken");

        Claims claims = jwtService.validar(token);
        assertEquals(CORREO, claims.getSubject());
        assertEquals("TRABAJADOR", claims.get("role", String.class));
    }

    @Test
    void correoSeNormaliza() throws Exception {
        crearTrabajador();
        mockMvc.perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(body("  MIGUEL@Hotmail.COM  ", PASSWORD)))
                .andExpect(status().isOk());
    }

    // PT6

    @Test
    void passwordErronea() throws Exception {
        crearTrabajador();
        mockMvc.perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(body(CORREO, "incorrecta123")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.accesoToken").doesNotExist());
    }

    @Test
    void correoInexistente() throws Exception {
        mockMvc.perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(body("hola@gmail.com", PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.accesoToken").doesNotExist());
    }

    @Test
    void mismoMensajeEnAmbosFallos() throws Exception {
        crearTrabajador();
        String conPasswordMala = login(body(CORREO, "incorrecta123"));
        String conCorreoFalso = login(body("hola@gmail.com", PASSWORD));

        String m1 = JsonPath.read(conPasswordMala, "$.mensaje");
        String m2 = JsonPath.read(conCorreoFalso, "$.mensaje");
        assertEquals(m1, m2);
    }

    @Test
    void sinPassword() throws Exception {
        mockMvc.perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"" + CORREO + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void correoMalFormado() throws Exception {
        mockMvc.perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(body("no-es-correo", PASSWORD)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cuerpoIlegible() throws Exception {
        mockMvc.perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content("{ esto no es json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void passwordGuardadaComoHash() throws Exception {
        crearTrabajador();
        String hash = repository.findByCorreo(CORREO).orElseThrow().getPassword_hash();
        assertNotEquals(PASSWORD, hash);
        assertTrue(hash.startsWith("$2"));
    }
}