import java.util.ArrayList;
import java.util.List;


public class Player {

    private final List<Item> inventory = new ArrayList<>();
    private Room currentRoom;

    public Player(Room startingRoom) {
        this.currentRoom = startingRoom;
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(Room currentRoom) {
        this.currentRoom = currentRoom;
    }

    public List<Item> getInventory() {
        return inventory;
    }

    public void pickUp(Item item) {
        inventory.add(item);
    }

    public String describeInventory() {
        if (inventory.isEmpty()) {
            return "Tvůj inventář je prázdný.";
        }
        StringBuilder sb = new StringBuilder("Inventář:");
        for (Item item : inventory) {
            sb.append("\n  - ").append(item);
        }
        return sb.toString();
    }
}
