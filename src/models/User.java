package models;

public class User {
    private final int userId;
    private final String username;
    private final String fullName;
    public User(int userId, String username, String fullName) {
        this.userId = userId; this.username = username; this.fullName = fullName;
    }
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
}
