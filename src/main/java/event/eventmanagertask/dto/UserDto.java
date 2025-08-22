package event.eventmanagertask.dto;

import event.eventmanagertask.model.Role;

public record UserDto (
        Long id,
        String login,
        Role role,
        Integer age
) {
}
