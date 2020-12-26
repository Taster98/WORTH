package worth;

public final class Constants {
    public static final String dbPath = "worth/server/Database/userDb.json";
    public static final int TCP_PORT = 6666;
    public static final String LOCALHOST_IP = "127.0.0.1";
    public static final String IP_CHAT = ""; // da scegliere
    public static final String HELP = "Welcome to WORTH.\n" +
            "Here is the complete command list:\n" +
            "help -> show this command list.\n" +
            "clean -> clear the window from things" +
            "register [nickname] [password] -> register a new user with [nickname] and [password]\n" +
            "login [nickname] [password] -> login as the user [nickname] with password [password]\n" +
            "logout -> logout from worth\n" +
            "exit -> exit from the program";
}
