package za.co.ooh.marketplace.auth.domains.user.services;

import za.co.ooh.marketplace.auth.domains.user.entity.UserEntity;
import za.co.ooh.marketplace.auth.domains.user.enums.Role;

import java.util.Optional;

public interface UserService {
    Optional<UserEntity> findUserByEmail(String email);

    boolean verifyPassword(String rawPassword, String encodedPassword);

    boolean verifyEmail(String token);

    void sendPasswordResetEmail(String email, String token);

    void sendVerifyEmail(String email, String token);

    UserEntity createUser(String email, String phoneNumber, String password, Role role);

    String login(String email, String password);

    void forgotPassword(String email);

    void resetPassword(String token, String password);
}
