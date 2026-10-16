package pl.witold.taskhub.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import pl.witold.taskhub.user.User;
import pl.witold.taskhub.user.UserRepository;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager, UserRepository userRepository, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow();

        String token = jwtService.generateToken(user);

        return new AuthResponse(token, toCurrentUserResponse(user));
    }

    public CurrentUserResponse me(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow();

        return toCurrentUserResponse(user);
    }

    private CurrentUserResponse toCurrentUserResponse(User user) {
        return new CurrentUserResponse(
                user.getId(),
                user.getUsername(),
                user.getRole().name()
        );
    }
}
