package za.co.ooh.marketplace.auth.domains.user.enums;

public enum Role {
    ADMIN("ADMIN"),
    OWNER("OWNER"),
    AGENT("AGENT"),
    PAINTER("PAINTER"),
    FLIGHTER("FLIGHTER"),
    CLIENT("CLIENT");

    private String role;

    Role(String role) {
        this.role = role;
    }
}
