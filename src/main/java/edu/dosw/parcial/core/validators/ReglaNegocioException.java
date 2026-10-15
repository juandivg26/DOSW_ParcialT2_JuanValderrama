package edu.dosw.parcial.core.validators;

import lombok.Getter;

@Getter
public class ReglaNegocioException extends RuntimeException {
    private final String code;

    public ReglaNegocioException(String code, String message) {
        super(message);
        this.code = code;
    }
}
