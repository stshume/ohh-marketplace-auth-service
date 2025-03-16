package za.co.ooh.marketplace.auth.domains.user.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import za.co.ooh.marketplace.auth.domains.user.dto.*;
import za.co.ooh.marketplace.auth.domains.user.entity.UserEntity;
import za.co.ooh.marketplace.auth.domains.user.enums.Role;
import za.co.ooh.marketplace.auth.domains.user.services.UserService;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {
    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Nested
    class LoginTests {

        @Test
        void shouldLoginSuccessfully() {
            var userDto = new UserLoginDto("test@email.com", "12345678");

            when(userService.login(userDto.email(), userDto.password())).thenReturn("FakeToken");

            ResponseEntity<Map<String, String>> response = userController.login(userDto);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("FakeToken", response.getBody().get("token"));
        }

        @Test
        void shouldFailLoginWithInvalidCredentials() {
            var userDto = new UserLoginDto("test@email.com", "wrongPassword");

            when(userService.login(userDto.email(), userDto.password()))
                    .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid login credentials"));

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> userController.login(userDto));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
            assertEquals("Invalid login credentials", exception.getReason());
        }
    }

    @Nested
    class RegisterTests {
        @Test
        void shouldRegisterUserSuccessfully() {
            var userDto = new UserDto("test@email.com", "0811234567","12345", Role.AGENT.name());

            when(userService.createUser(userDto.email(), userDto.phoneNumber(), userDto.password(),
                    Role.valueOf(userDto.role()))).thenReturn(
                            new UserEntity(userDto.email(),
                                    userDto.phoneNumber(),
                                    userDto.password(),
                                    Role.valueOf(userDto.role())));
            ResponseEntity<Map<String, UserEntity>> response = userController.register(userDto);
            verify(userService, times(1)).createUser(userDto.email(), userDto.phoneNumber(), userDto.password(), Role.valueOf(userDto.role()));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("test@email.com", response.getBody().get("user").getEmail());
        }

        @Test
        void shouldFailRegisterIfEmailAlreadyExists() {
            var userDto = new UserDto("test@email.com", "0811234567", "12345", Role.AGENT.name());

            doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists"))
                    .when(userService).createUser(userDto.email(), userDto.phoneNumber(), userDto.password(), Role.valueOf(userDto.role()));

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> userController.register(userDto));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
            assertEquals("Email already exists", exception.getReason());
        }

    }

    @Nested
    class RedeemPassword {
        @Test
        void shouldRedeemPasswordSuccessfully() {
            // Arrange
            String email = "test@example.com";
            UserForgotPasswordDto userDto = new UserForgotPasswordDto(email);
            doNothing().when(userService).forgotPassword(email);

            // Act
            ResponseEntity<Map<String, String>> response = userController.redeemPassword(userDto);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("A password reset link has been sent to your email", response.getBody().get("message"));
            Mockito.verify(userService, Mockito.times(1)).forgotPassword(email);
        }

        @Test
        void shouldNotRedeemPasswordDueToInvalidEmail() {
            String email = "invalid-email";
            UserForgotPasswordDto userDto = new UserForgotPasswordDto(email);

            doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email"))
                    .when(userService).forgotPassword(email);

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> userController.redeemPassword(userDto));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
            assertEquals("Invalid email", exception.getReason());

            verify(userService, times(1)).forgotPassword(email);

        }

        @Test
        void shouldNotRedeemPasswordDueToEmailNotFound() {

            String email = "notfound@example.com";
            UserForgotPasswordDto userDto = new UserForgotPasswordDto(email);

            doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email not found"))
                    .when(userService).forgotPassword(email);

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> userController.redeemPassword(userDto));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
            assertEquals("Email not found", exception.getReason());

            verify(userService, times(1)).forgotPassword(email);

        }
    }

    @Nested
    class ResetPassword {

        @Test
        void shouldResetPasswordSuccessfully() {

            String token = "valid-token";
            String newPassword = "newSecurePassword123!";
            UserResetPasswordDto userDto = new UserResetPasswordDto(token, newPassword);

            ResponseEntity<Map<String, String>> response = userController.resetPassword(userDto);

            verify(userService, times(1)).resetPassword(token, newPassword);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("Credentials updated", response.getBody().get("message"));
        }

        @Test
        void shouldFailResetPasswordDueToInvalidToken() {

            String token = "invalid-token";
            String newPassword = "newSecurePassword123!";
            UserResetPasswordDto userDto = new UserResetPasswordDto(token, newPassword);

            doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid token"))
                    .when(userService).resetPassword(token, newPassword);

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> userController.resetPassword(userDto));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
            assertEquals("Invalid token", exception.getReason());

            verify(userService, times(1)).resetPassword(token, newPassword);
        }

        @Test
        void shouldFailResetPasswordDueToExpiredToken() {

            String token = "expired-token";
            String newPassword = "newSecurePassword123!";
            UserResetPasswordDto userDto = new UserResetPasswordDto(token, newPassword);

            doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token expired"))
                    .when(userService).resetPassword(token, newPassword);

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> userController.resetPassword(userDto));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
            assertEquals("Token expired", exception.getReason());

            verify(userService, times(1)).resetPassword(token, newPassword);
        }
    }

    @Nested
    class VerifyEMail {

        @Test
        void shouldVerifyEmailSuccessfully() {

            String token = "valid-token";
            when(userService.verifyEmail(token)).thenReturn(true);
            ResponseEntity<Map<String, String>> response = userController.verifyEmail(token);

            verify(userService, times(1)).verifyEmail(token);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("Email has been verified.", response.getBody().get("message"));
        }

        @Test
        void shouldFailVerifyEmailDueToInvalidToken() {

            String token = "invalid-token";
            doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid token"))
                    .when(userService).verifyEmail(token);

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> userController.verifyEmail(token));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
            assertEquals("Invalid token", exception.getReason());

            verify(userService, times(1)).verifyEmail(token);
        }

    }

}
