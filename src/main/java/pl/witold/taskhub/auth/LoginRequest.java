package pl.witold.taskhub.auth;

public record LoginRequest(
        String username,
        String password
) {}