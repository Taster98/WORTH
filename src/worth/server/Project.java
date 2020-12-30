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
    public synchronized List<Card> getAllCards(String projectName){
        Gson gson = new Gson();
        BufferedReader br;
        try {
            //Devo leggere tutte le 4 liste di cards
            List<Card> lista1, lista2, lista3, lista4;
            br = new BufferedReader(new FileReader(Constants.progettiPath+projectName+"/todoList.json"));
            Type type = new TypeToken<ArrayList<Card>>() {
            }.getType();
            lista1 = gson.fromJson(br, type);
            br = new BufferedReader(new FileReader(Constants.progettiPath+projectName+"/progressList.json"));
            lista2 = gson.fromJson(br,type);
            br = new BufferedReader(new FileReader(Constants.progettiPath+projectName+"/tobeRevisedList.json"));
            lista3 = gson.fromJson(br, type);
            br = new BufferedReader(new FileReader(Constants.progettiPath+projectName+"/doneList.json"));
            lista4 = gson.fromJson(br, type);

            List<Card> result = new ArrayList<>();
            if(lista1 != null)
                result.addAll(lista1);
            if(lista2 != null)
                result.addAll(lista2);
            if(lista3 != null)
                result.addAll(lista3);
            if(lista4 != null)
                result.addAll(lista4);
            return result;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized String getCardList(String projectName){
        Gson gson = new Gson();
        BufferedReader br;
        try {
            //Devo leggere tutte le 4 liste di cards
            List<Card> lista1, lista2, lista3, lista4;
            br = new BufferedReader(new FileReader(Constants.progettiPath+projectName+"/todoList.json"));
            Type type = new TypeToken<ArrayList<Card>>() {
            }.getType();
            lista1 = gson.fromJson(br, type);
            br = new BufferedReader(new FileReader(Constants.progettiPath+projectName+"/progressList.json"));
            lista2 = gson.fromJson(br,type);
            br = new BufferedReader(new FileReader(Constants.progettiPath+projectName+"/tobeRevisedList.json"));
            lista3 = gson.fromJson(br, type);
            br = new BufferedReader(new FileReader(Constants.progettiPath+projectName+"/doneList.json"));
            lista4 = gson.fromJson(br, type);

            List<String> result = new ArrayList<>();
            for(Card c : lista1){
                result.add(c.getCardName());
            }
            for(Card c : lista2){
                result.add(c.getCardName());
            }
            for(Card c : lista3){
                result.add(c.getCardName());
            }
            for(Card c : lista4){
                result.add(c.getCardName());
            }

            //converto tutta la lista in un'unica gigantesca stringa
            String output = "";
            for(String s : result){
                output += s + "?";
            }
            return output;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized String getCardInfo(String projectName, String cardName){
        Gson gson = new Gson();
        BufferedReader br;
        try {
            //Devo leggere tutte le 4 liste di cards
            List<Card> lista1, lista2, lista3, lista4;
            br = new BufferedReader(new FileReader(Constants.progettiPath+projectName+"/todoList.json"));
            Type type = new TypeToken<ArrayList<Card>>() {
            }.getType();
            lista1 = gson.fromJson(br, type);
            br = new BufferedReader(new FileReader(Constants.progettiPath+projectName+"/progressList.json"));
            lista2 = gson.fromJson(br,type);
            br = new BufferedReader(new FileReader(Constants.progettiPath+projectName+"/tobeRevisedList.json"));
            lista3 = gson.fromJson(br, type);
            br = new BufferedReader(new FileReader(Constants.progettiPath+projectName+"/doneList.json"));
            lista4 = gson.fromJson(br, type);

            Card c = new Card(cardName);
            boolean found = false;
            if(lista1.contains(c)){
                c = lista1.get(lista1.indexOf(c));
                found = true;
            }else if(lista2.contains(c)){
                c = lista1.get(lista1.indexOf(c));
                found = true;
            }else if(lista3.contains(c)){
                c = lista1.get(lista1.indexOf(c));
                found = true;
            }else if(lista4.contains(c)){
                c = lista1.get(lista1.indexOf(c));
                found = true;
            }
            if(found){
                return "card name: "+c.getCardName()+"?card description: "+c.getCardDescription()+"?current list: "+c.getCurrentListName()+"?";
            }else{
                return null;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized String getCardHistory(String projectName, String cardName){
        Gson gson = new Gson();
        BufferedReader br;
        try {
            //Devo leggere tutte le 4 liste di cards
            List<Card> lista1, lista2, lista3, lista4;
            br = new BufferedReader(new FileReader(Constants.progettiPath+projectName+"/todoList.json"));
            Type type = new TypeToken<ArrayList<Card>>() {
            }.getType();
            lista1 = gson.fromJson(br, type);
            br = new BufferedReader(new FileReader(Constants.progettiPath+projectName+"/progressList.json"));
            lista2 = gson.fromJson(br,type);
            br = new BufferedReader(new FileReader(Constants.progettiPath+projectName+"/tobeRevisedList.json"));
            lista3 = gson.fromJson(br, type);
            br = new BufferedReader(new FileReader(Constants.progettiPath+projectName+"/doneList.json"));
            lista4 = gson.fromJson(br, type);

            Card c = new Card(cardName);
            boolean found = false;
            if(lista1.contains(c)){
                c = lista1.get(lista1.indexOf(c));
                found = true;
            }else if(lista2.contains(c)){
                c = lista1.get(lista1.indexOf(c));
                found = true;
            }else if(lista3.contains(c)){
                c = lista1.get(lista1.indexOf(c));
                found = true;
            }else if(lista4.contains(c)){
                c = lista1.get(lista1.indexOf(c));
                found = true;
            }
            if(found){
                String res = "";
                for(String s : c.getHistory()){
                    res += s + "?";
                }
                return res;
            }else{
                return null;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized boolean doCardAlreadyExist(String projectName, String cardName){
        Card c = new Card(cardName);
        List<Card> aux;
        if((aux = getAllCards(projectName)) != null){
            return aux.contains(c);
        }
        return false;
    }
}
