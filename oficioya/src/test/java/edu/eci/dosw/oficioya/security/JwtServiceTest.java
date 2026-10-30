package edu.eci.dosw.oficioya.security;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.IncorrectClaimException;
import io.jsonwebtoken.JwtException;

class JwtServiceTest {

    private static final String SECRET = "CxmLyz5e9uR2Ywd7M9gCD7K94KKd3GVCGd+CZaOvoFJlxa3yihlH5nsJqB4Q/Lp6";
    private static final String ISSUER = "oficioya";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, ISSUER, 30);
    }

    @Test
    void generaYValidaTokenConLosClaimsDeLaRegla3() {
        Claims claims = jwtService.validar(jwtService.generar("a@b.com", "TRABAJADOR"));

        assertEquals("a@b.com", claims.getSubject());
        assertEquals("TRABAJADOR", claims.get("role", String.class));
        assertEquals("oficioya", claims.getIssuer());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void tokenFirmadoConHS256() {
        String token = jwtService.generar("a@b.com", "ADMIN");
        String header = new String(Base64.getUrlDecoder().decode(token.split("\\.")[0]), StandardCharsets.UTF_8);

        assertTrue(header.contains("HS256"));
    }

    @Test
    void tokenDuraTreintaMinutos() {
        Claims claims = jwtService.validar(jwtService.generar("a@b.com", "ADMIN"));

        long duracionMs = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();
        assertEquals(30 * 60 * 1000L, duracionMs);
    }

    @Test
    void tokenVencidoSeRechaza() {
        JwtService vencido = new JwtService(SECRET, ISSUER, -1);
        String token = vencido.generar("a@b.com", "ADMIN");

        ExpiredJwtException ex = assertThrows(ExpiredJwtException.class, () -> jwtService.validar(token));
        assertNotNull(ex.getClaims());
    }

    @Test
    void tokenConFirmaAlteradaSeRechaza() {
        String token = jwtService.generar("a@b.com", "ADMIN");
        String alterado = token.substring(0, token.length() - 2) + "xx";

        JwtException ex = assertThrows(JwtException.class, () -> jwtService.validar(alterado));
        assertNotNull(ex.getMessage());
    }

    @Test
    void tokenDeOtroEmisorSeRechaza() {
        String token = new JwtService(SECRET, "otro-emisor", 30).generar("a@b.com", "ADMIN");

        IncorrectClaimException ex = assertThrows(IncorrectClaimException.class, () -> jwtService.validar(token));
        assertEquals("iss", ex.getClaimName());
    }
}