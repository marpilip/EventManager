package event.eventmanagertask.controller;

import event.eventmanagertask.SignInRequest;
import event.eventmanagertask.SignUpRequest;
import event.eventmanagertask.dto.UserDto;
import event.eventmanagertask.jwt.JwtTokenResponse;
import event.eventmanagertask.model.User;
import event.eventmanagertask.service.AuthenticationService;
import event.eventmanagertask.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final AuthenticationService authenticationService;

    public UserController(UserService userService, AuthenticationService authenticationService) {
        this.userService = userService;
        this.authenticationService = authenticationService;
    }

    @PostMapping()
    public ResponseEntity<UserDto> register(@RequestBody @Validated SignUpRequest signUpRequest) {
        User createdUser = userService.registerUser(signUpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(convertDomainUser(createdUser));
    }

    @PostMapping("/auth")
    public ResponseEntity<JwtTokenResponse> authenticate(@RequestBody @Validated SignInRequest signInRequest) {
        String token = authenticationService.authenticateUser(signInRequest);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new JwtTokenResponse(token));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserInfo(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(convertDomainUser(user));
    }

    private UserDto convertDomainUser(User user) {
        return new UserDto(
                user.id(),
                user.login(),
                user.role(),
                user.age()
        );
    }
}
