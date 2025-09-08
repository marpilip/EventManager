package event.eventmanagertask.model;

public record User(
        Long id,
        String login,
        Role role,
        String passwordHash,
        Integer age
) {
    public static User fromTokenData(Long id, String login, Role role) {
        return new User(id, login, role, null, null);
    }

    public static User withAllData(Long id, String login, Role role, String passwordHash, Integer age) {
        return new User(id, login, role, passwordHash, age);
    }
}
