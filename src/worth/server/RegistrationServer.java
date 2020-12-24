package worth.server;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import worth.Constants;
import worth.RegistrationInterface;

import java.io.*;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

public class RegistrationServer implements RegistrationInterface {
    List<User> userDb;

    public RegistrationServer() {
        // leggo il file json e carico la lista
        initDb();
    }

    @Override
    public String register(String nickUtente, String password) throws RemoteException {
        // Nickname e password non devono essere nè null nè vuoti
        if (nickUtente == null || password == null) throw new NullPointerException();
        if (nickUtente.equals("") || password.equals("")) throw new IllegalArgumentException();

        //La password va hashata, per non memorizzarla in chiaro
        try {
            String newPwd = hashMe(password);
            // Creo ora un utente
            User user = new User();
            user.setNickName(nickUtente);
            user.setPassword(newPwd);
            // controllo che la lista non sia vuota
            if(userDb == null) userDb = new ArrayList<>();
            // aggiungo l'utente alla lista solo se non esiste già
            if(!userDb.contains(user))
                userDb.add(user);
            else return "User " + nickUtente +" already registered!";
            // salvo la lista nel file json
            Writer writer = new FileWriter(Constants.dbPath);
            Gson gson = new GsonBuilder().create();
            gson.toJson(userDb, writer);
            writer.flush();
            writer.close();
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }

        return "User " + nickUtente + " registered!";
    }

    private void initDb() {
        Gson gson = new Gson();
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(Constants.dbPath));
            Type type = new TypeToken<List<User>>() {
            }.getType();
            userDb = gson.fromJson(br, type);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String hashMe(String pwd) throws NoSuchAlgorithmException {
        // Genero l'hash SHA-256 della stringa pwd, di modo da non salvarla in chiaro
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        // Converto il byte array in un signum
        BigInteger num = new BigInteger(1, md.digest(pwd.getBytes(StandardCharsets.UTF_8)));
        // Converto il messaggio digest in numero in esadecimale
        StringBuilder hexStr = new StringBuilder(num.toString(16));
        // Aggiungo eventualmente del padding (le stringhe hash devono avere tutte la stessa lunghezza)
        while (hexStr.length() < 32) {
            hexStr.insert(0, '0');
        }
        // A questo punto ho il mio hash della password
        return hexStr.toString();
    }

    public static void main(String[] args) {
        try {
            RegistrationServer usr = new RegistrationServer();
            // Creo lo stub per la registrazione:
            RegistrationInterface stub = (RegistrationInterface) UnicastRemoteObject.exportObject(usr, 0);

            // faccio il bind dell'oggetto remoto nel registry
            Registry registry = LocateRegistry.getRegistry();
            registry.bind("RegistrationInterface", stub);

            System.err.println("Server pronto.");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }

    }
}
