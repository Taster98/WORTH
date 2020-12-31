package worth.server;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class Card {
    private String cardName;
    private String cardDescription;
    private String currentListName;
    private CopyOnWriteArrayList<String> history;

    public Card(String cardName){
        this.cardName = cardName;
        this.currentListName = "todoList";
        history = new CopyOnWriteArrayList<>();
        //Il primo evento di ogni card è sempre todoList
        history.addIfAbsent("todoList");
    }

    public synchronized void setCardDescription(String desc){
        this.cardDescription = desc;
    }

    public synchronized void setCurrentListName(String currentListName) {
        if(!(this.currentListName.equals("todoList") && this.history.size() == 1)){
            history.add(this.currentListName);
        }
        this.currentListName = currentListName;
        //history.add(this.currentListName);
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
