package worth;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RegistrationInterface extends Remote {
    int register(String nickUtente, String password) throws RemoteException;
}