package za.co.ooh.marketplace.auth.domains.user.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserDto(
        @Email @NotBlank 
        String email,
        @Digits(integer = 10, fraction = 0) @NotBlank
        String phoneNumber,
        @NotBlank
        String password,
        @NotBlank
        String role
) {
}
