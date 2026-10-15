package edu.dosw.parcial.core.validators;

import lombok.Getter;

@Getter
public class ConflictoException extends RuntimeException {
    private final String code;

    public ConflictoException(String code, String message) {
        super(message);
        this.code = code;
    }
}
