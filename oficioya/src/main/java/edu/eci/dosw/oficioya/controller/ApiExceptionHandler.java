package edu.eci.dosw.oficioya.exception;

import edu.eci.dosw.oficioya.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(CorreoDuplicadoException.class)
    public ResponseEntity<ApiError> duplicado(CorreoDuplicadoException excepcion, HttpServletRequest registro) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ApiError.of(409, "Conflict", "El correo ya está registrado", registro.getRequestURI()));
    }

    @ExceptionHandler(RolNoPermitidoException.class)
    public ResponseEntity<ApiError> rolNoPermitido(RolNoPermitidoException excepcion, HttpServletRequest registro) {
        return ResponseEntity.badRequest().body(
                ApiError.of(400, "Bad Request", excepcion.getMessage(), registro.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> datosInvalidos(MethodArgumentNotValidException excepcion, HttpServletRequest registro) {
        return ResponseEntity.badRequest().body(
                ApiError.of(400, "Bad Request", "Datos inválidos", registro.getRequestURI()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> cuerpoIlegible(HttpMessageNotReadableException excepcion, HttpServletRequest registro) {
        return ResponseEntity.badRequest().body(
                ApiError.of(400, "Bad Request", "Cuerpo de la petición inválido", registro.getRequestURI()));
    }
}