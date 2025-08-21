package event.eventmanagertask.mapper;

import event.eventmanagertask.dto.UserDto;
import event.eventmanagertask.entity.UserEntity;
import event.eventmanagertask.model.Role;
import event.eventmanagertask.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserEntityMapper {

    public UserEntity toEntity(User user) {
        return new UserEntity(
                user.id(),
                user.login(),
                user.passwordHash(),
                user.role(),
                user.age()
        );
    }

    public User toDomain(UserEntity entity) {
        return new User(
                entity.getId(),
                entity.getLogin(),
                entity.getRole(),
                entity.getPasswordHash(),
                entity.getAge()
        );
    }

}
