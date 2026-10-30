package edu.eci.dosw.oficioya.exception;

public class CorreoDuplicadoException extends RuntimeException {
    public CorreoDuplicadoException(String correo){
        super("Su correo " + correo + "ya está registrado");
    }
}
