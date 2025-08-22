package event.eventmanagertask.mapper;

import event.eventmanagertask.dto.UserDto;
import event.eventmanagertask.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserDto convertDomainUser(User user) {
        return new UserDto(
                user.id(),
                user.login(),
                user.role(),
                user.age()
        );
    }
}
