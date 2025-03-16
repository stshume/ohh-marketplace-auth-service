package za.co.ooh.marketplace.auth.domains.user.entity;

import java.time.Instant;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import za.co.ooh.marketplace.auth.domains.user.enums.Role;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "user", schema = "ooh_db")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String email;
    private Boolean emailVerified = false;
    private String password;
    private String phoneNumber;
    private Boolean phoneNumberVerified = false;
    private String resetToken;
    private Instant resetTokenExpiration;
    private Boolean locked = false;
    private LocalDateTime lockedAt;
    private Boolean deleted = false;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;
    @Enumerated(EnumType.STRING)
    private Role roles;

    public UserEntity(String email, String password, Role roles) {
        this.email = email;
        this.password = password;
        this.roles = roles;
    }

    public UserEntity(String email, String phoneNumber, String password, Role roles) {
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.roles = roles;
    }

    public UserEntity withResetToken(String resetToken, Instant resetTokenAdditionalTime) {
        this.resetToken = resetToken;
        this.resetTokenExpiration = resetTokenAdditionalTime;
        return this;
    }

}
