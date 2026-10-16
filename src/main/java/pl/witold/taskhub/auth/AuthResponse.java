package pl.witold.taskhub.auth;

public record AuthResponse(
        String token,
        CurrentUserResponse user
) {}