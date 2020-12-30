package worth.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import worth.Constants;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CopyOnWriteArrayList;

/*
* nomeProgetto + todoList<Card>, progressList<Card>, tobeRevisedList<Card>, doneList<Card> + userList<User>
* */
public class Project {
    private String projectName;
    private CopyOnWriteArrayList<Card> todoList;
    private CopyOnWriteArrayList<Card> progressList;
    private CopyOnWriteArrayList<Card> tobeRevisedList;
    private CopyOnWriteArrayList<Card> doneList;
    private CopyOnWriteArrayList<String> userList;

    public Project(String projectName){
        this.projectName = projectName;
        this.todoList = new CopyOnWriteArrayList<Card>();
        this.progressList = new CopyOnWriteArrayList<Card>();
        this.tobeRevisedList = new CopyOnWriteArrayList<Card>();
        this.doneList = new CopyOnWriteArrayList<Card>();
        this.userList = new CopyOnWriteArrayList<String>();
    }

    public synchronized boolean addUser(String usr){
        return this.userList.addIfAbsent(usr);
    }
    public synchronized CopyOnWriteArrayList<String> getUserList(){
        return this.userList;
    }
    public synchronized String createDir(String projectName){
        try {
            Path path = Paths.get(Constants.progettiPath+projectName+"/");
            Files.createDirectories(path);
            return path.toString();
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    public synchronized boolean generateLists(String path){
        try{
            File todo = new File(path+"/todoList.json");
            File progress = new File(path+"/progressList.json");
            File tobe = new File(path+"/tobeRevisedList.json");
            File done = new File(path+"/doneList.json");
            File usrs = new File(path+"/userList.json");
            return (todo.createNewFile() && progress.createNewFile() && tobe.createNewFile() && done.createNewFile() && usrs.createNewFile());
        }catch(IOException e){
            e.printStackTrace();
        }
        return false;
    }

    //funzione che scrive nel file con la lista di utenti del progetto
    public synchronized void readUserList(String path){
        Gson gson = new Gson();
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(path+"/userList.json"));
            Type type = new TypeToken<CopyOnWriteArrayList<String>>() {
            }.getType();
            userList = gson.fromJson(br, type);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public synchronized boolean writeUserList(String path){
        Writer writer;
        try {
            writer = new FileWriter(path+"/userList.json");
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(userList, writer);
            writer.flush();
            writer.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
