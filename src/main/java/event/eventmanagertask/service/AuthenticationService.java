package event.eventmanagertask.service;

import event.eventmanagertask.SignInRequest;
import event.eventmanagertask.jwt.JwtTokenManager;
import event.eventmanagertask.model.User;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenManager jwtTokenManager;

    public AuthenticationService(AuthenticationManager authenticationManager,
                                 JwtTokenManager jwtTokenManager) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenManager = jwtTokenManager;
    }

    public String authenticateUser(SignInRequest signInRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        signInRequest.login(),
                        signInRequest.password()
                )
        );

        return jwtTokenManager.generateToken(signInRequest.login());
    }

    public User getCurrentAuthenticatedUserOrThrow() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("Нет авторизации");
        }

        return (User) authentication.getPrincipal();
    }
}
