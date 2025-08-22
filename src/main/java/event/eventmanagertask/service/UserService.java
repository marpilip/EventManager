package event.eventmanagertask.service;

import event.eventmanagertask.SignUpRequest;
import event.eventmanagertask.entity.UserEntity;
import event.eventmanagertask.exception.UserAlreadyExistsException;
import event.eventmanagertask.mapper.UserEntityMapper;
import event.eventmanagertask.model.Role;
import event.eventmanagertask.model.User;
import event.eventmanagertask.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserEntityMapper userEntityMapper;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       UserEntityMapper userEntityMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userEntityMapper = userEntityMapper;
    }

    public User registerUser(SignUpRequest signUpRequest) {
        if (userRepository.existsByLogin(signUpRequest.login())) {
            throw new UserAlreadyExistsException(
                    "Пользователь с логином: " + signUpRequest.login() + " уже существует");
        }

        String hashedPassword = passwordEncoder.encode(signUpRequest.password());

        UserEntity userToSave = new UserEntity(
                null,
                signUpRequest.login(),
                hashedPassword,
                Role.USER,
                signUpRequest.age()
        );

        UserEntity savedUser = userRepository.save(userToSave);

        return mapToDomain(savedUser);
    }

    public User findByLogin(String login) {
        UserEntity user = userRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        return mapToDomain(user);
    }

    public User saveUser(User user) {
        UserEntity entity = userEntityMapper.toEntity(user);
        UserEntity savedUser = userRepository.save(entity);
        return userEntityMapper.toDomain(savedUser);
    }

    private static User mapToDomain(UserEntity userEntity) {
        return new User(
                userEntity.getId(),
                userEntity.getLogin(),
                userEntity.getRole(),
                userEntity.getPasswordHash(),
                userEntity.getAge()
        );
    }

    public boolean isUserExistsByLogin(String login) {
        return userRepository.findByLogin(login)
                .isPresent();
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .map(userEntityMapper::toDomain)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с id: " + userId + " не найден"));
    }
}
