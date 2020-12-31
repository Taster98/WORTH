package worth.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import worth.Constants;

import java.io.*;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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
    //Aggiungo l'ip per il multicast
    private InetAddress multicastIp;
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

    public synchronized boolean addTodoList(Card c) {
        if(todoList == null)
            this.todoList = new CopyOnWriteArrayList<>();
        return this.todoList.addIfAbsent(c);
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
    public synchronized boolean removeDir(File dir){
        //File dir = new File(Constants.progettiPath+projectName+"/");
        File[] content = dir.listFiles();
        if (content != null) {
            for (File file : content) {
                removeDir(file);
            }
        }
        return dir.delete();
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
    public synchronized boolean writeTodoList(String path){
        Writer writer;
        try {
            writer = new FileWriter(path+"/todoList.json");
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(todoList, writer);
            writer.flush();
            writer.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
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
    public synchronized boolean writeAllLists(String path){
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
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    public synchronized List<Card> getAllCards(){
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
    public synchronized List<Card> getDoneCards(){
        if(this.doneList == null)
            this.doneList = new CopyOnWriteArrayList<>();
        return this.doneList;
    }
    public synchronized String getCardList(){
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

    public synchronized String getCardInfo(String cardName){
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
    public synchronized int moveCard(String cardName, String srcList, String destList){
        CopyOnWriteArrayList<Card> listSrc;
        CopyOnWriteArrayList<Card> listDest;
        if(srcList.equals("todoList")){
            if(this.todoList == null)
                this.todoList = new CopyOnWriteArrayList<>();
            listSrc = this.todoList;
        }else if(srcList.equals("progressList")){
            if(this.progressList == null)
                this.progressList = new CopyOnWriteArrayList<>();
            listSrc = this.progressList;
        }else if(srcList.equals("revisedList")){
            if(this.tobeRevisedList == null)
                this.tobeRevisedList = new CopyOnWriteArrayList<>();
            listSrc = this.tobeRevisedList;
        }else if(srcList.equals("doneList")){
            if(this.doneList == null)
                this.doneList = new CopyOnWriteArrayList<>();
            listSrc = this.doneList;
        }else{
            listSrc = null;
        }

        if(destList.equals("todoList")){
            if(this.todoList == null)
                this.todoList = new CopyOnWriteArrayList<>();
            listDest = this.todoList;
        }else if(destList.equals("progressList")){
            if(this.progressList == null)
                this.progressList = new CopyOnWriteArrayList<>();
            listDest = this.progressList;
        }else if(destList.equals("revisedList")){
            if(this.tobeRevisedList == null)
                this.tobeRevisedList = new CopyOnWriteArrayList<>();
            listDest = this.tobeRevisedList;
        }else if(destList.equals("doneList")){
            if(this.doneList == null)
                this.doneList = new CopyOnWriteArrayList<>();
            listDest = this.doneList;
        }else{
            listDest = null;
        }
        if(listSrc != null && listDest != null){
            //Controllo che le liste siano diverse; se fossero uguali non sposto nulla
            if(listSrc != listDest){
                //controllo che la lista src contiene la card da spostare
                Card c = new Card(cardName);
                if(listSrc.contains(c)){
                    c = listSrc.get(listSrc.indexOf(c));
                    c.setCurrentListName(destList);
                    listDest.addIfAbsent(c);
                    //writeAllLists(Constants.progettiPath+projectName);
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

    public synchronized String getCardHistory(String cardName){
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

    public synchronized boolean doCardAlreadyExist(String cardName){
        Card c = new Card(cardName);
        List<Card> aux;
        if((aux = getAllCards()) != null){
            return aux.contains(c);
        }
        return false;
    }
}
