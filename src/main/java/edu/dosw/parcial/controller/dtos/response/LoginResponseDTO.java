package edu.dosw.parcial.controller.dtos.response;

public record LoginResponseDTO(String accessToken, String tokenType, long expiresIn, String rol) {
}
