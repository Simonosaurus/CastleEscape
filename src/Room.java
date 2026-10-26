import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Jedna lokace herního světa. Drží své východy a předměty, které v ní leží.
 */
public class Room {

    private final int id;
    private final String name;
    private final Map<String, Room> exits = new HashMap<>();   // klíč je směr "N", "E", "S", "W"
    private final List<Item> items = new ArrayList<>();
    private final List<Monster> monsters = new ArrayList<>();
    private String description = "";
    private boolean winRoom;
    private String welcomeMessage = "";

    public Room(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isWinRoom() {
        return winRoom;
    }

    public void setWinRoom(boolean winRoom) {
        this.winRoom = winRoom;
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public void setWelcomeMessage(String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
    }

    public List<Monster> getMonsters() {
        return monsters;
    }

    public void addMonster(Monster monster) {
        monsters.add(monster);
    }

    public void removeMonster(Monster monster) {
        monsters.remove(monster);
    }

    public void addExit(String direction, Room target) {
        exits.put(direction.toUpperCase(), target);
    }

    public Room getExit(String direction) {
        return exits.get(direction.toUpperCase());
    }

    public List<Item> getItems() {
        return items;
    }

    public void addItem(Item item) {
        items.add(item);
    }

    public void removeItem(Item item) {
        items.remove(item);
    }

    public Item findItem(String itemName) {
        for (Item item : items) {
            if (item.getName().equalsIgnoreCase(itemName)) {
                return item;
            }
        }
        return null;
    }

    public String describeItems() {
        if (items.isEmpty()) {
            return "V místnosti nic neleží.";
        }
        StringBuilder sb = new StringBuilder("V místnosti leží:");
        for (Item item : items) {
            sb.append("\n  - ").append(item);
        }
        return sb.toString();
    }
}
