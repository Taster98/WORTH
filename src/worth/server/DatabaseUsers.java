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
    public synchronized void readDb() {
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
    public synchronized boolean addUser(User usr) {
        if(userDb == null)
            userDb = new CopyOnWriteArrayList<>();
        return userDb.addIfAbsent(usr);
    }
    // metodo che scrive sul file le modifiche al database degli utenti
    public synchronized int writeDb() {
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

    public synchronized boolean cercaUtente(User usr){
        if(this.userDb != null)
            return this.userDb.contains(usr);
        else return false;
    }
    public synchronized void setStatus(User usr, String status){
        userDb.get(userDb.indexOf(usr)).setStato(status);
        writeDb();
    }
    public synchronized CopyOnWriteArrayList<User> getUserDb() {
        return userDb;
    }

    public synchronized String getListStatus(){
        String result = "";
        for(User u : userDb){
            result += u.getNickName() + ": "+u.getStato() + "?";
        }
        return result;
    }

    public synchronized String getOnlineListStatus(){
        String result = "";
        for(User u : userDb){
            if(u.getStato().equals("online"))
                result += u.getNickName() + ": "+u.getStato() + "?";
        }
        return result;
    }

    public synchronized int dbLength(){
        return userDb.size();
    }
}
