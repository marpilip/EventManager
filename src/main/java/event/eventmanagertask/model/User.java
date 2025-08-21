package event.eventmanagertask.model;

public record User(
        Long id,
        String login,
        Role role,
        String passwordHash,
        Integer age
) {
}
