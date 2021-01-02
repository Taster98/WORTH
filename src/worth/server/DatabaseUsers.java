package worth.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import worth.Constants;

import java.io.*;
import java.lang.reflect.Type;
import java.util.concurrent.CopyOnWriteArrayList;

/* Questa classe è utilizzata per la gestione del database utenti, garantisce la thread-safeness tramite strutture dati
concorrenti e accessi in mutua esclusione ad esse e al file dbUsers.json.
 */
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
    public synchronized void writeDb() {
        Writer writer;
        try {
            writer = new FileWriter(Constants.dbPath);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(userDb, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //metodo che cerca un utente nella struttura dati
    public boolean cercaUtente(User usr){
        if(this.userDb != null)
            return this.userDb.contains(usr);
        else return false;
    }

    //metodo osservatore che preleva l'utente dalla struttura dati
    public User getUtente(User usr){
        if(this.userDb != null){
            return userDb.get(userDb.indexOf(usr));
        }
        return null;
    }

    //Metodo che cambia lo stato dell'utente
    public synchronized void setStatus(User usr, String status){
        userDb.get(userDb.indexOf(usr)).setStato(status);
        writeDb();
    }

    //Metodo osservatore che ritorna la struttura dati contenente la lista utenti
    public CopyOnWriteArrayList<User> getUserDb() {
        return userDb;
    }

    //Metodo che ritorna una stringa contenente tutti gli utenti e i loro stati.
    public String getListStatus(){
        String result = "";
        for(User u : userDb){
            result += u.getNickName() + ": "+u.getStato() + "£";
        }
        result = result.subSequence(0, result.length()-1).toString();
        return result;
    }

    //metodo che aggiunge un progetto a un utente
    public synchronized boolean addProject(User usr, String projectName){
        User aux = userDb.get(userDb.indexOf(usr));
        if(aux.getProjectList() == null){
           aux.setProjectList(new CopyOnWriteArrayList<>());
        }
        boolean res = aux.getProjectList().addIfAbsent(projectName);
        // Rimuovo dal database e lo reinserisco
        userDb.remove(aux);
        userDb.addIfAbsent(aux);
        writeDb();
        return res;
    }

    //metodo che rimuove un progetto da un utente
    public synchronized void removeProject(User usr, String projName){
        User aux = userDb.get(userDb.indexOf(usr));
        if(aux.getProjectList() == null){
            aux.setProjectList(new CopyOnWriteArrayList<>());
        }
        aux.getProjectList().remove(projName);
        //Cancello il progetto da tutti i membri e non solo da chi chiama la remove:
        for(User u : userDb){
            u.getProjectList().remove(projName);
        }
        // Rimuovo dal database e lo reinserisco
        userDb.remove(aux);
        userDb.addIfAbsent(aux);
        writeDb();
    }

    //metodo che restituisce una stringa con la lista dei progetti dell'utente
    public String getUserProjectList(User usr){
        CopyOnWriteArrayList<String> lista = getUtente(usr).getProjectList();
        String res = "";
        for(String s : lista){
            res += s + "£";
        }
        if(res.length() >0)
            res = res.subSequence(0,res.length()-1).toString();
        return res;
    }

    //metodo che controlla la presenza di un progetto nella lista progetti dell'utente
    public boolean isMember(String projName, User usr){
        CopyOnWriteArrayList<String> lista = getUtente(usr).getProjectList();
        return lista.contains(projName);
    }

    //metodo che ritorna la lista di membri di un progetto
    public String getMemberList(String projName){
        CopyOnWriteArrayList<String> lista;
        String fullPath = Constants.progettiPath + projName;
        Project p = new Project(projName);
        p.readUserList(fullPath);
        lista = p.getUserList();
        String res = "";
        for(String s : lista){
            res += s + "£";
        }
        if(res.length() >0)
                res = res.subSequence(0, res.length()-1).toString();
        return res;
    }

    //metodo che aggiunge un utente alla lista utenti di un progetto
    public synchronized boolean addMemberToList(String projName, User usr){
        CopyOnWriteArrayList<String> list;
        String fullPath = Constants.progettiPath + projName;
        Project p = new Project(projName);
        p.readUserList(fullPath);
        list = p.getUserList();
        boolean res;
        res = list.addIfAbsent(usr.getNickName());
        p.writeUserList(fullPath);
        // Devo ora scriverlo anche nel file classico, nella lista progetti globale
        readDb();
        addProject(usr, projName);
        return res;
    }
}
