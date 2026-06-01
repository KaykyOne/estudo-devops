package br.com.fatecads.fatecads.dto;

public record LoginResponse(String token, String type, long expiresInSeconds) {
}
