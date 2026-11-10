package pl.witold.taskhub.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication
        .AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AuthenticationCredentialsNotFoundException(
                    "User is not authenticated"
            );
        }

        return userRepository
                .findByUsername(authentication.getName())
                .filter(User::isEnabled)
                .orElseThrow(() ->
                        new AuthenticationCredentialsNotFoundException(
                                "User is not available"
                        )
                );
    }
}