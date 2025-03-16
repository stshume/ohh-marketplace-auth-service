package za.co.ooh.marketplace.auth.domains.user.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record UserLoginDto(
        @Email @NotBlank 
        String email,
        @NotBlank
        String password
) {
}
