package worth.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import worth.Constants;

import java.io.*;
import java.lang.reflect.Type;
import java.util.concurrent.CopyOnWriteArrayList;

public class DatabaseUsers {
    CopyOnWriteArrayList<User> userDb;

    public DatabaseUsers() {
        userDb = new CopyOnWriteArrayList<>();
    }
    // metodo che legge dal file al db
    public void readDb() {
        Gson gson = new Gson();
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(Constants.dbPath));
            Type type = new TypeToken<CopyOnWriteArrayList<User>>() {
            }.getType();
            userDb = gson.fromJson(br, type);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    // metodo che aggiunge un utente, se assente
    public boolean addUser(User usr) {
        return userDb.addIfAbsent(usr);
    }
    // metodo che scrive sul file le modifiche al database degli utenti
    public int writeDb() {
        Writer writer;
        try {
            writer = new FileWriter(Constants.dbPath);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(userDb, writer);
            writer.flush();
            writer.close();
            return 1;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public boolean cercaUtente(User usr){
        return this.userDb.contains(usr);
    }

    public CopyOnWriteArrayList<User> getUserDb() {
        return userDb;
    }
}
