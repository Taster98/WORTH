package worth;

import java.rmi.Remote;
import java.rmi.RemoteException;
/*
* RMI interface for users registration.
*
* @author Luigi Gesuele
* */
public interface RegistrationInterface extends Remote {
    int register(String nickUtente, String password) throws RemoteException;
}
