package edu.dosw.parcial.core.services;

import edu.dosw.parcial.controller.dtos.request.CrearMascotaRequestDTO;
import edu.dosw.parcial.controller.dtos.response.MascotaResponseDTO;

public interface IMascotaService {
    MascotaResponseDTO crearMascota(CrearMascotaRequestDTO request);
}
