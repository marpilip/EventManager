package event.eventmanagertask.model;

import event.eventmanagertask.service.UserService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DefaultUserInitializer {
    private static final Logger log = LoggerFactory.getLogger(DefaultUserInitializer.class);

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public DefaultUserInitializer(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void initUsers() {
        log.info("Начато создание базовых пользователей, при их отсуствии");

        try {
            createUserIfNotExists("admin", "admin", Role.ADMIN, 30);
            createUserIfNotExists("user", "user", Role.USER, 25);
            log.info("Базовые пользователи добавлены успешно");
        } catch (Exception e) {
            log.error("ОШибка при создании базовых пользователей ", e);
            throw e;
        }
    }

    private void createUserIfNotExists(String login, String password, Role role, Integer age) {
        if (userService.isUserExistsByLogin(login)) {
            return;
        }

        String hashedPass = passwordEncoder.encode(password);
        User user = new User(
                null,
                login,
                role,
                hashedPass,
                age
        );

        userService.saveUser(user);
    }
}
