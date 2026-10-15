package edu.dosw.parcial.core.validators;

public class CredencialesInvalidasException extends RuntimeException {
    public CredencialesInvalidasException() {
        super("Credenciales inválidas. Verifique su correo y contraseña.");
    }
}
