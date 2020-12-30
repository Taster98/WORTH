package worth.server;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class User {
    private String nickName;
    private String password;
    // offline o online, serve per poi restituire la lista di online/offline
    private String stato;
    // lista progetti di cui l'utente è membro
    private CopyOnWriteArrayList<String> projectList;

    public User(){
        this.nickName = "";
        this.password = "";
        this.stato = "offline";
        this.projectList = new CopyOnWriteArrayList<>();
    }

    public String getNickName() {
        return nickName;
    }

    public String getPassword() {
        return password;
    }

    public String getStato() {
        return stato;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }

    public CopyOnWriteArrayList<String> getProjectList() {
        return projectList;
    }

    public void setProjectList(CopyOnWriteArrayList<String> projectList) {
        this.projectList = projectList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return nickName.equals(user.nickName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nickName, password);
    }

    public boolean passwordMatch(String psw){
        return psw.equals(getPassword());
    }
}
