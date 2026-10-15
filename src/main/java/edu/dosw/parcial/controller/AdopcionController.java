package edu.dosw.parcial.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.dosw.parcial.controller.dtos.response.ConsultarAdopcionesResponseDTO;
import edu.dosw.parcial.core.services.IAdopcionService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Validated
@RestController
@RequestMapping("/api/v1/adopciones")
public class AdopcionController {

    private final IAdopcionService adopcionService;

    public AdopcionController(IAdopcionService adopcionService) {
        this.adopcionService = adopcionService;
    }

    @GetMapping
    public ConsultarAdopcionesResponseDTO consultarAdoptadas(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return adopcionService.consultarAdoptadas(page, size);
    }
}
