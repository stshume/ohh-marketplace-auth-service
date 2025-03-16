package za.co.ooh.marketplace.auth.domains.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UserVerifyEmailDto(@NotBlank String token) {

}
