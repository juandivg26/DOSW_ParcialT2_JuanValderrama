package edu.dosw.parcial.core.validators;

import lombok.Getter;

@Getter
public class RecursoNoEncontradoException extends RuntimeException {
    private final String code;

    public RecursoNoEncontradoException(String code, String message) {
        super(message);
        this.code = code;
    }
}
