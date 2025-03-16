package za.co.ooh.marketplace.auth.domains.user.services.impl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import za.co.ooh.marketplace.auth.domains.email.services.EmailService;
import za.co.ooh.marketplace.auth.domains.user.entity.UserEntity;
import za.co.ooh.marketplace.auth.domains.user.enums.Role;
import za.co.ooh.marketplace.auth.domains.user.repositories.UserRepository;
import za.co.ooh.marketplace.auth.domains.user.services.UserService;
import za.co.ooh.marketplace.auth.utils.JwtActions;

@Service
public class UserServiceImpl implements UserService {

    @Value("${token.expiration.seconds:300}")
    private Long tokenExpirationSeconds;

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    private final JwtActions jwtActions;

    private final EmailService emailService;

    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, JwtActions jwtActions,
                           EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtActions = jwtActions;
        this.emailService = emailService;
    }

    public Optional<UserEntity> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<UserEntity> findByResetToken(String token) {
        return userRepository.findByResetToken(token);
    }

    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public boolean verifyEmail(String token) {
        var user = findByResetToken(token).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "User not found."));
        boolean verified = token.equals(user.getResetToken());
        if(verified) {
            user.setEmailVerified(true);
            user.withResetToken(null, null);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        }
        return verified;
    }

    public void sendPasswordResetEmail(String email, String token) {
        String subject = "OOH Marketplace - Password Reset Request";
        String resetUrl = "http://localhost:8080/user/reset-password?token=" + token;
        String body = "Click the link to reset your password: " + resetUrl;
        emailService.sendEmail(email, subject, body);
    }

    public void sendVerifyEmail(String email, String token) {
        String subject = "OOH Marketplace - Email Verification";
        String verifyUrl = "http://localhost:8080/user/verify-email?token=" + token;
        String body = "Click the link to verify your email address: " + verifyUrl;
        emailService.sendEmail(email, subject, body);
    }

    @Override
    public UserEntity createUser(String email, String phoneNumber, String password, Role role) {

        if (findUserByEmail(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists !");
        }
        var encodedPassword = passwordEncoder.encode(password);
        var newUser = new UserEntity(email, phoneNumber, encodedPassword, role);
        newUser.setResetToken(UUID.randomUUID().toString());
        userRepository.save(newUser);

        return newUser;
    }

    @Override
    public String login(String email, String password) {
        var user = findUserByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Invalid login credentials"));
        if (!verifyPassword(password, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid login credentials");
        }
        return jwtActions.jwtCreate(user.getEmail(), user.getRoles().toString());

    }

    @Override
    public void forgotPassword(String email) {
        var user = findUserByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Invalid email"));
        var token = UUID.randomUUID().toString();
        user.withResetToken(token, Instant.now().plusSeconds(this.tokenExpirationSeconds));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        sendPasswordResetEmail(user.getEmail(), token);
    }

    @Override
    public void resetPassword(String token, String password) {
        var user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "User not found"));
        if (user.getResetTokenExpiration().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token expired");
        }
        user.setPassword(passwordEncoder.encode(password));
        user.withResetToken(null, null);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }
}
