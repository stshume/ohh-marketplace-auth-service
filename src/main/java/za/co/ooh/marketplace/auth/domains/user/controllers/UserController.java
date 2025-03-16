package za.co.ooh.marketplace.auth.domains.user.controllers;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import za.co.ooh.marketplace.auth.domains.user.dto.*;
import za.co.ooh.marketplace.auth.domains.user.entity.UserEntity;
import za.co.ooh.marketplace.auth.domains.user.enums.Role;
import za.co.ooh.marketplace.auth.domains.user.services.UserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("user")
public class UserController {

    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody @Valid UserLoginDto userDto) {
        var token = userService.login(userDto.email(), userDto.password());
        return ResponseEntity.ok().body(Map.of("token", token));
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, UserEntity>> register(@RequestBody @Valid UserDto userDto) {
        var userEntity = userService.createUser(userDto.email(), userDto.phoneNumber(), userDto.password(), Role.valueOf(userDto.role()));
        if(userEntity != null) {
            userService.sendVerifyEmail(userDto.email(), UUID.randomUUID().toString());
        }
        assert userEntity != null;
        return ResponseEntity.ok().body(Map.of("user", userEntity));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> redeemPassword(@RequestBody @Valid UserForgotPasswordDto userDto) {
        userService.forgotPassword(userDto.email());

        return ResponseEntity.ok().body(Map.of("message", "A password reset link has been sent to your email"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody @Valid UserResetPasswordDto userDto) {
        userService.resetPassword(userDto.token(), userDto.password());

        return ResponseEntity.ok().body(Map.of("message", "Credentials updated"));
    }

    @GetMapping("/verify-email/{token}")
    public ResponseEntity<Map<String, String>> verifyEmail(@PathVariable String token) {
        var userVerify = userService.verifyEmail(token);
        var response = "Email has been verified.";
        if(!userVerify) {
            response = "Could not verify email.";
        }
        return ResponseEntity.ok().body(Map.of("message", response));
    }

}
