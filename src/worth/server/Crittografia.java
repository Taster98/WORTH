package worth.server;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/*
Questa classe contiene un metodo statico per cifrare la password che un utente utilizza, al momento della registrazione
tramite una funzione di hashing che utilizza l'algoritmo SHA-256. Anche durante il login vengono quindi confrontati
gli hash delle password e non le password in chiaro; in questo modo, un'eventuale intrusione nel 'database' da parte di terzi
impedisce comunque il furto delle password degli utenti.
*/
public class Crittografia {
    public static String hashMe(String pwd)throws NoSuchAlgorithmException{
        // Genero l'hash SHA-256 della stringa pwd, di modo da non salvarla in chiaro
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        // Converto il byte array in un signum
        BigInteger num = new BigInteger(1, md.digest(pwd.getBytes(StandardCharsets.UTF_8)));
        // Converto il messaggio digest in numero in esadecimale
        StringBuilder hexStr = new StringBuilder(num.toString(16));
        // Aggiungo eventualmente del padding (le stringhe hash devono avere tutte la stessa lunghezza)
        while (hexStr.length() < 32) {
            hexStr.insert(0, '0');
        }
        // A questo punto ho il mio hash della password
        return hexStr.toString();
    }
}
