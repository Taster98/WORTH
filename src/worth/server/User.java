package worth.server;

import java.util.Objects;

public class User {
    private String nickName;
    private String password;

    public User(){
        this.nickName = "";
        this.password = "";
    }

    public String getNickName() {
        return nickName;
    }

    public String getPassword() {
        return password;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public void setPassword(String password) {
        this.password = password;
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
}
