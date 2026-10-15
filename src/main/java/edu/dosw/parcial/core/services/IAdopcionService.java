package edu.dosw.parcial.core.services;

import edu.dosw.parcial.controller.dtos.response.ConsultarAdopcionesResponseDTO;

public interface IAdopcionService {
    ConsultarAdopcionesResponseDTO consultarAdoptadas(int page, int size);
}
