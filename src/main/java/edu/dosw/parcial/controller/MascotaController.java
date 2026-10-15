package edu.dosw.parcial.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import edu.dosw.parcial.controller.dtos.request.CrearMascotaRequestDTO;
import edu.dosw.parcial.controller.dtos.response.MascotaResponseDTO;
import edu.dosw.parcial.core.services.IMascotaService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/mascotas")
public class MascotaController {

    private final IMascotaService mascotaService;

    public MascotaController(IMascotaService mascotaService) {
        this.mascotaService = mascotaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MascotaResponseDTO crearMascota(@Valid @RequestBody CrearMascotaRequestDTO request) {
        return mascotaService.crearMascota(request);
    }
}
