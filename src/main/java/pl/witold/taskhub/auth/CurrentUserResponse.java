package pl.witold.taskhub.auth;

public record CurrentUserResponse(
        Long id,
        String username,
        String role
) {}
