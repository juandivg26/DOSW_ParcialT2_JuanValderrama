package edu.dosw.parcial.controller.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import edu.dosw.parcial.controller.dtos.response.ErrorResponseDTO;
import edu.dosw.parcial.core.validators.ConflictoException;
import edu.dosw.parcial.core.validators.RecursoNoEncontradoException;
import edu.dosw.parcial.core.validators.ReglaNegocioException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponseDTO recursoNoEncontrado(RecursoNoEncontradoException ex) {
        return new ErrorResponseDTO(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(ConflictoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponseDTO conflicto(ConflictoException ex) {
        return new ErrorResponseDTO(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorResponseDTO reglaNegocio(ReglaNegocioException ex) {
        return new ErrorResponseDTO(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO validacionDto(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .distinct()
                .collect(java.util.stream.Collectors.joining("; "));
        if (message.isBlank()) {
            message = "Los datos enviados no son válidos.";
        }
        return new ErrorResponseDTO(codigoDatosInvalidos(request), message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO cuerpoInvalido(HttpServletRequest request) {
        return new ErrorResponseDTO(codigoDatosInvalidos(request),
                "El cuerpo de la petición falta o contiene valores inválidos.");
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class, ConstraintViolationException.class,
            HandlerMethodValidationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO parametrosInvalidos() {
        return new ErrorResponseDTO("PARAMETROS_INVALIDOS", "Los parámetros enviados no son válidos.");
    }

    private String codigoDatosInvalidos(HttpServletRequest request) {
        return "POST".equals(request.getMethod())
                && "/api/v1/mascotas".equals(request.getServletPath()) ? "E02" : "DATOS_INVALIDOS";
    }
}
