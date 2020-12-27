package worth;

public final class Constants {
    /*Colori CLI*/
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static final String dbPath = "worth/server/Database/userDb.json";
    public static final int TCP_PORT = 6666;
    public static final String LOCALHOST_IP = "127.0.0.1";
    public static final String IP_CHAT = ""; // da scegliere
    public static final String HELP = "Welcome to WORTH.\n" +
            "Here is the complete command list:\n" +
            "help -> show this command list.\n" +
            "clear -> clear the window from things\n" +
            "register [nickname] [password] -> register a new user with [nickname] and [password]\n" +
            "login [nickname] [password] -> login as the user [nickname] with password [password]\n" +
            "logout -> logout from worth\n" +
            "users -> list users registered to worth\n" +
            "online -> list only online registered users" +
            "exit -> exit from the program";
}
