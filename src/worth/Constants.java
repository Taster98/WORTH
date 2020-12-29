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

    //Percorso database utenti registrati
    public static final String dbPath = "worth/server/Database/userDb.json";
    //Percorso database dei progetti creati
    public static final String progettiPath = "worth/server/Database/progetti/";
    //Percorso del file dei progetti esistenti
    public static final String fileProgettiPath = progettiPath+"listaProgetti.json";

    //Porta del server TCP
    public static final int TCP_PORT = 6666;
    //Indirizzo del server TCP (in questo caso è l'indirizzo locale)
    public static final String LOCALHOST_IP = "127.0.0.1";

    //Testo del comando di help
    public static final String HELP = "Welcome to WORTH.\n" +
            "Here is the complete command list:\n\n" +
            "\u001B[35m register [nickname] [password] \u001B[0m -> register a new user with [nickname] and [password]\n" +
            "\u001B[35m login [nickname] [password] \u001B[0m -> login as the user [nickname] with password [password]\n" +
            "\u001B[35m logout [nickname] \u001B[0m -> sign out the user [nickname] from worth\n" +
            "\u001B[35m users \u001B[0m -> list users registered to worth\n" +
            "\u001B[35m online \u001B[0m -> list only online registered users\n" +
            "\u001B[35m help \u001B[0m -> show this command list.\n" +
            "\u001B[35m version \u001B[0m -> show current version of WORTH.\n" +
            "\u001B[35m clear \u001B[0m -> clear the window from things\n" +
            "\u001B[35m exit \u001B[0m -> exit from the program";
    //Testo del comando della versione
    public static final String VERSION = "\u001B[36m WORTH version 0.1\n \u001B[32m 2020 Luigi Gesuele \u001B[0m";
}
