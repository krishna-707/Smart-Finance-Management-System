package utils;

public class Session {
    private static int userId;
    private static String username;
    private static String fullName;

    public static void start(int id, String user, String name) {
        userId = id;
        username = user;
        fullName = name;
    }
    public static int getUserId() { return userId; }
    public static String getUsername() { return username; }
    public static String getFullName() { return fullName; }
    public static void end() { userId = 0; username = null; fullName = null; }
}
