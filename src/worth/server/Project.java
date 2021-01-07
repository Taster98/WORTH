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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/*
Classe che rappresenta un'entità progetto, composto di:
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
        this.todoList = new CopyOnWriteArrayList<>();
        this.progressList = new CopyOnWriteArrayList<>();
        this.tobeRevisedList = new CopyOnWriteArrayList<>();
        this.doneList = new CopyOnWriteArrayList<>();
        this.userList = new CopyOnWriteArrayList<>();
    }
    //Metodo che aggiunge un utente al progetto
    public synchronized void addUser(String usr){
        this.userList.addIfAbsent(usr);
    }

    //metodo che restituisce la lista utenti di un progetto
    public CopyOnWriteArrayList<String> getUserList(){
        return this.userList;
    }

    //metodo che aggiunge card alla lista todoList
    public synchronized boolean addTodoList(Card c) {
        if(todoList == null)
            this.todoList = new CopyOnWriteArrayList<>();
        return this.todoList.addIfAbsent(c);
    }

    //Metodo che crea la directory relativa al progetto
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

    //Metodo che rimuove la directory relativa al progetto e il suo contenuto
    public synchronized boolean removeDir(File dir){
        File[] content = dir.listFiles();
        if (content != null) {
            for (File file : content) {
                removeDir(file);
            }
        }
        return dir.delete();
    }

    //Metodo che genera la lista di file relativa alle liste di un progetto
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

    //funzione che legge dal file la lista di utenti del progetto
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
    //funzione che scrive nel file con la lista di utenti del progetto
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

    //funzione che reperisce la lista delle cards nella todoList:
    public synchronized void readTodoList(String path){
        Gson gson = new Gson();
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(path+"/todoList.json"));
            Type type = new TypeToken<CopyOnWriteArrayList<Card>>() {
            }.getType();
            todoList = gson.fromJson(br, type);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    //funzione che scrive nella todoList
    public synchronized void writeTodoList(String path){
        Writer writer;
        try {
            writer = new FileWriter(path+"/todoList.json");
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(todoList, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //funzione che legge tutte le liste di cards
    public synchronized void readAllLists(String path){
        Gson gson = new Gson();
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(path+"/todoList.json"));
            Type type = new TypeToken<CopyOnWriteArrayList<Card>>() {
            }.getType();
            todoList = gson.fromJson(br, type);
            br = new BufferedReader(new FileReader(path+"/progressList.json"));
            progressList = gson.fromJson(br, type);
            br = new BufferedReader(new FileReader(path+"/tobeRevisedList.json"));
            tobeRevisedList = gson.fromJson(br, type);
            br = new BufferedReader(new FileReader(path+"/doneList.json"));
            doneList = gson.fromJson(br, type);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    //funzione che scrive tutte le liste di cards
    public synchronized void writeAllLists(String path){
        Writer writer;
        try {
            writer = new FileWriter(path+"/todoList.json");
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            if(todoList != null) {
                gson.toJson(todoList, writer);
            }
            writer.flush();
            writer.close();
            writer = new FileWriter(path+"/progressList.json");
            gson = new GsonBuilder().setPrettyPrinting().create();
            if(progressList != null) {
                gson.toJson(progressList, writer);
            }
            writer.flush();
            writer.close();
            writer = new FileWriter(path+"/tobeRevisedList.json");
            gson = new GsonBuilder().setPrettyPrinting().create();
            if(tobeRevisedList != null) {
                gson.toJson(tobeRevisedList, writer);
            }
            writer.flush();
            writer.close();
            writer = new FileWriter(path+"/doneList.json");
            gson = new GsonBuilder().setPrettyPrinting().create();
            if(doneList != null) {
                gson.toJson(doneList, writer);
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Metodo che ritorna la lista di tutte le card
    public List<Card> getAllCards(){
        List<Card> result = new ArrayList<>();
        if(todoList != null)
            result.addAll(todoList);
        if(progressList != null)
            result.addAll(progressList);
        if(tobeRevisedList != null)
            result.addAll(tobeRevisedList);
        if(doneList != null)
            result.addAll(doneList);
        return result;
    }

    //Metodo che ritorna la lista di card nella doneList
    public List<Card> getDoneCards(){
        if(this.doneList == null)
            this.doneList = new CopyOnWriteArrayList<>();
        return this.doneList;
    }

    //Metodo che restituisce una stringa stampabile della lista di tutte le card.
    public String getCardList(){
        List<String> result = new ArrayList<>();
        if(todoList != null) {
            for (Card c : todoList) {
                result.add(c.getCardName());
            }
        }
        if(progressList != null) {
            for (Card c : progressList) {
                result.add(c.getCardName());
            }
        }
        if(tobeRevisedList != null) {
            for (Card c : tobeRevisedList) {
                result.add(c.getCardName());
            }
        }
        if(doneList != null) {
            for (Card c : doneList) {
                result.add(c.getCardName());
            }
        }
        //converto tutta la lista in un'unica gigantesca stringa
        String output = "";
        for(String s : result){
            output += s + "£";
        }
        return output;
    }

    //Metodo che restituisce tutte le informazioni di una specifica card.
    public String getCardInfo(String cardName){
        Card c = new Card(cardName);
        boolean found = false;
        if(todoList != null && todoList.contains(c)){
            c = todoList.get(todoList.indexOf(c));
            found = true;
        }else if(progressList != null && progressList.contains(c)){
            c = progressList.get(progressList.indexOf(c));
            found = true;
        }else if(tobeRevisedList != null && tobeRevisedList.contains(c)){
            c = tobeRevisedList.get(tobeRevisedList.indexOf(c));
            found = true;
        }else if(doneList != null && doneList.contains(c)){
            c = doneList.get(doneList.indexOf(c));
            found = true;
        }
        if(found){
            return "card name: "+c.getCardName()+"£card description: "+c.getCardDescription()+"£current list: "+c.getCurrentListName()+"£";
        }else{
            return null;
        }
    }

    //Metodo che sposta una card da una lista a un'altra
    public synchronized int moveCard(String cardName, String srcList, String destList){
        CopyOnWriteArrayList<Card> listSrc;
        CopyOnWriteArrayList<Card> listDest;
        if(srcList.equals("todo")){
            if(this.todoList == null)
                this.todoList = new CopyOnWriteArrayList<>();
            listSrc = this.todoList;
        }else if(srcList.equals("progress")){
            if(this.progressList == null)
                this.progressList = new CopyOnWriteArrayList<>();
            listSrc = this.progressList;
        }else if(srcList.equals("revised")){
            if(this.tobeRevisedList == null)
                this.tobeRevisedList = new CopyOnWriteArrayList<>();
            listSrc = this.tobeRevisedList;
        }else if(srcList.equals("done")){
            if(this.doneList == null)
                this.doneList = new CopyOnWriteArrayList<>();
            listSrc = this.doneList;
        }else{
            listSrc = null;
        }

        if(destList.equals("todo")){
            if(this.todoList == null)
                this.todoList = new CopyOnWriteArrayList<>();
            listDest = this.todoList;
        }else if(destList.equals("progress")){
            if(this.progressList == null)
                this.progressList = new CopyOnWriteArrayList<>();
            listDest = this.progressList;
        }else if(destList.equals("revised")){
            if(this.tobeRevisedList == null)
                this.tobeRevisedList = new CopyOnWriteArrayList<>();
            listDest = this.tobeRevisedList;
        }else if(destList.equals("done")){
            if(this.doneList == null)
                this.doneList = new CopyOnWriteArrayList<>();
            listDest = this.doneList;
        }else{
            listDest = null;
        }
        if(listSrc != null && listDest != null){
            //Controllo che le liste siano diverse; se fossero uguali non sposto nulla
            if(listSrc != listDest){
                //Controllo i vari possibili casi di spostamento: todolist->progress, progress->done || revised, revised->progress || done
                boolean admitted = ((srcList.equals("todo") && destList.equals("progress")) || (srcList.equals("progress") && (destList.equals("done") || destList.equals("revised"))) || (srcList.equals("revised") && (destList.equals("progress") || destList.equals("done"))));
                if(!admitted) return -6; //Se la condizione è falsa esco senza spostare, altrimenti continua dopo
                //controllo che la lista src contiene la card da spostare
                Card c = new Card(cardName);
                if(listSrc.contains(c)){
                    c = listSrc.get(listSrc.indexOf(c));
                    c.setCurrentListName(destList);
                    listDest.addIfAbsent(c);
                    listSrc.remove(c);
                    writeAllLists(Constants.progettiPath+projectName);
                }else{
                    return 0;
                }
            }
            return 7;
        }else{
            return -1;
        }
    }
    //Metodo che ritorna la history di una card, sottoforma di stringa
    public String getCardHistory(String cardName){
        Card c = new Card(cardName);
        boolean found = false;
        if(todoList != null && todoList.contains(c)){
            c = todoList.get(todoList.indexOf(c));
            found = true;
        }else if(progressList != null && progressList.contains(c)){
            c = progressList.get(progressList.indexOf(c));
            found = true;
        }else if(tobeRevisedList != null && tobeRevisedList.contains(c)){
            c = tobeRevisedList.get(tobeRevisedList.indexOf(c));
            found = true;
        }else if(doneList != null && doneList.contains(c)){
            c = doneList.get(doneList.indexOf(c));
            found = true;
        }
        if(found){
            String res = "";
            for(String s : c.getHistory()){
                res += s + "£";
            }
            return res;
        }else{
            return null;
        }
    }

    //Metodo che controlla l'esistenza di una card
    public boolean doCardAlreadyExist(String cardName){
        Card c = new Card(cardName);
        List<Card> aux;
        if((aux = getAllCards()) != null){
            return aux.contains(c);
        }
        return false;
    }
}
