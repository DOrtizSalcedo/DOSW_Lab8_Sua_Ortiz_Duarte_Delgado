package edu.eci.dosw.oficioya.dto;

public record LoginResponse(String tipoToken,
                            String accesoToken,
                            long expiraEn) {
}
