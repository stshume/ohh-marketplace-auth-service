package za.co.ooh.marketplace.auth.domains.user.dto;

import jakarta.validation.constraints.Email;

public record UserForgotPasswordDto(@Email String email) {

}
