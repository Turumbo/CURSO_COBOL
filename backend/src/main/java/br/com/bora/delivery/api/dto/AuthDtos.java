package br.com.bora.delivery.api.dto;

import br.com.bora.delivery.domain.AccountRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Email @Size(max = 160) String email,
            @NotBlank @Size(min = 8, max = 72) String password,
            @Size(max = 30) String phone) {
    }

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password) {
    }

    public record UpdateProfileRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Email @Size(max = 160) String email,
            @Size(max = 30) String phone,
            @Size(max = 240) String deliveryAddress) {
    }

    public record AccountDto(
            Long id, String name, String email, AccountRole role, String phone, String deliveryAddress) {
    }

    public record SessionDto(String token, AccountDto user) {
    }
}
