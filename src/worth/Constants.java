package worth;

public final class Constants {
    /*Colori CLI*/
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";

    //Percorso database utenti registrati
    public static final String dbPath = "worth/server/Database/userDb.json";
    //Percorso database dei progetti creati
    public static final String progettiPath = "worth/server/Database/progetti/";
    //Percorso del file dei progetti esistenti
    public static final String fileProgettiPath = progettiPath+"listaProgetti.json";
    //Percorso del file degli indirizzi ip
    public static final String ipAddressPath = progettiPath+"listaIpAddress.json";
    //Percorso lista utenti da aggiornare client
    public static final String userListPath = "worth/client/userList.txt";

    //Porta del server TCP
    public static final int TCP_PORT = 6666;
    //Indirizzo del server TCP (in questo caso è l'indirizzo locale)
    public static final String LOCALHOST_IP = "127.0.0.1";
    //Base per l'indirizzo multicast LOCALE
    public static final String CHAT_IP_BASE = "239.0.0.0";
    public static final int UDP_PORT = 4321;

    //Testo del comando di help
    public static final String HELP = "Welcome to WORTH.\n" +
            "Here is the complete command list: \u001B[34m NOTICE: COMMANDS ARE NOT CASE SENSITIVE \u001B[0m \n\n" +
            "\u001B[35m register [nickname] [password] \u001B[0m -> register a new user with [nickname] and [password]\n" +
            "\u001B[35m login [nickname] [password] \u001B[0m -> login as the user [nickname] with password [password]\n" +
            "\u001B[35m logout [nickname] \u001B[0m -> sign out the user [nickname] from worth\n" +
            "\u001B[35m users \u001B[0m -> list users registered to worth\n" +
            "\u001B[35m online \u001B[0m -> list only online registered users\n" +
            "\u001B[35m createProject [projectName] \u001B[0m -> create a new project\n" +
            "\u001B[35m listProjects \u001B[0m -> list all projects of current user\n" +
            "\u001B[35m addMember [projectName] [nickname] \u001B[0m -> add [nickname] user to [projectName] project\n" +
            "\u001B[35m showMembers [projectName] \u001B[0m -> list all users of [projectName] project\n" +
            "\u001B[35m showCards [projectName] \u001B[0m -> list all cards of [projectName] project\n" +
            "\u001B[35m showCard [projectName] [cardName] \u001B[0m -> show informations about [cardName] card in [projectName] project\n" +
            "\u001B[35m addCard [projectName] [cardName] [description] \u001B[0m -> add [cardName] to [projectName] with [description]\n" +
            "\u001B[35m moveCard [projectName] [cardName] {todo, progress, revised, done} {todo, progress, revised, done} \u001B[0m -> moves [cardName] of [projectName] project from [srcList] to [destList].\n" + //TODO
            "\u001B[35m getHistory [projectName] [cardName] \u001B[0m -> show the history of [cardName] card in [projectName] project.\n" +
            "\u001B[35m readChat [projectName] \u001B[0m -> show the chat of the project [projectName].\n" +
            "\u001B[35m sendMsg [projectName] [message] \u001B[0m -> Send [message] to [projectName] project chat.\n" +
            "\u001B[35m cancelProject [projectName] \u001B[0m -> delete project [projectName].\n" +
            "\u001B[35m help \u001B[0m -> show this command list.\n" +
            "\u001B[35m version \u001B[0m -> show current version of WORTH.\n" +
            "\u001B[35m clear \u001B[0m -> clear the window from things\n" +
            "\u001B[35m exit \u001B[0m -> exit from the program";
    //Testo del comando della versione
    public static final String VERSION = "\u001B[36m WORTH version 0.1\n \u001B[32m 2020 Luigi Gesuele \u001B[0m";
}
