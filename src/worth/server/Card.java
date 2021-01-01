package worth.server;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

//Questo oggetto identifica una card di un progetto.
public class Card {
    private String cardName;
    private String cardDescription;
    private String currentListName;

    //La history è stata pensata come una lista contenente tutte le liste attraverso la quale la card è passata, ad
    //eccezione di quella corrente, contenuta nella variabile d'istanza 'currentListName'.
    private CopyOnWriteArrayList<String> history;

    //Quando una card viene creata, viene inserita nella todoList.
    public Card(String cardName){
        this.cardName = cardName;
        this.currentListName = "todoList";
        history = new CopyOnWriteArrayList<>();
        //Il primo evento di ogni card è sempre todoList
        history.addIfAbsent("todoList");
    }

    //I metodi hanno la clausola "synchronized" poiché la struttura dati può essere acceduta concorrentemente da più thread.

    public synchronized void setCardDescription(String desc){
        this.cardDescription = desc;
    }

    public synchronized void setCurrentListName(String currentListName) {
        if(!(this.currentListName.equals("todoList") && this.history.size() == 1)){
            history.add(this.currentListName);
        }
        this.currentListName = currentListName;
    }

    public synchronized String getCardName() {
        return cardName;
    }

    public synchronized String getCardDescription() {
        return cardDescription;
    }

    public synchronized String getCurrentListName() {
        return currentListName;
    }

    public synchronized CopyOnWriteArrayList<String> getHistory() {
        return history;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return Objects.equals(cardName, card.cardName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardName);
    }
}
