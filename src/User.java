import java.util.Objects;

public abstract class User {
    private final long userId;
    private String name;
    private final String email;
    private AccountStatus status;

    protected User(long userId, String name, String email) {
        if (userId <= 0) throw new IllegalArgumentException("userId must be positive.");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name must not be blank.");
        if (email == null || email.isBlank()) throw new IllegalArgumentException("email must not be blank.");

        this.userId = userId;
        this.name = name;
        this.email = email;
        this.status = AccountStatus.ACTIVE;
    }

    public long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name must not be blank.");
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = AccountStatus.DEACTIVATED;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
                "{id=" + userId +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", status=" + status +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return userId == user.userId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}