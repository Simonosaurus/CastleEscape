import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Player {

    private static final int MIN_HEALTH_AFTER_DISCARD = 1;
    private final Stats stats = Stats.starting();
    private final List<Item> inventory = new ArrayList<>();
    private final List<Weapon> equippedWeapons = new ArrayList<>();
    private Room currentRoom;
    private Room previousRoom;

    public Player(Room startingRoom) {
        this.currentRoom = startingRoom;
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(Room currentRoom) {
        if(this.currentRoom != null && this.currentRoom != currentRoom){
            this.previousRoom = this.currentRoom;
        }
        this.currentRoom = currentRoom;
    }

    public Room getPreviousRoom() {
        return previousRoom;
    }

    public void setPreviousRoom(Room previousRoom) {
        this.previousRoom = previousRoom;
    }

    public Stats getStats() {
        return stats;
    }

    public List<Item> getInventory() {
        return inventory;
    }

    public List<Weapon> getEquippedWeapons() {
        return Collections.unmodifiableList(equippedWeapons);
    }
    public int getWeaponDamage(){
        int sum = 0;
        for(Weapon weapon : equippedWeapons){
            sum += weapon.getDamage();
        }
        return sum;
    }

    public String pickUp(Item item) {
        inventory.add(item);
        if(item instanceof Weapon weapon){
            equippedWeapons.add(weapon);
        }
        return applyModifier(item, 1);
    }
    public String discard(Item item){
        inventory.remove(item);
        if(item instanceof Weapon weapon){
            equippedWeapons.remove(weapon);
        }
        return applyModifier(item, -1);
    }
    public Item findItem(String itemName){
        for(Item item : inventory){
            if(item.getName().equalsIgnoreCase(itemName)){
                return item;
            }
        }
        return null;
    }
    public void restoreItem(Item item){
        inventory.add(item);
        if(item instanceof Weapon weapon){
            equippedWeapons.add(weapon);
        }
    }

    public String describeInventory() {
        StringBuilder sb = new StringBuilder();
        if (inventory.isEmpty()) {
            sb.append("Tvůj inventář je prázdný.");
        }else{
            sb.append("Inventář:");
            for (Item item : inventory) {
                sb.append("\n  - ").append(item);
            }
        }
        sb.append("\nVlastnosti:");
        for (String stat : Stats.NAMES){
            String label = Stats.labelCZ(stat);
            sb.append("\n ").append(Character.toUpperCase(label.charAt(0))).append(label.substring(1)).append(": ").append(stats.get(stat));
        }
        return sb.toString();
    }


    private String applyModifier(Item item, int sign){
        Stats modifier = item.getModifier();
        List<String> lines = new ArrayList<>();
        for(String stat : Stats.NAMES){
            int delta = modifier.get(stat) * sign;
            if(delta == 0){
                continue;
            }
            int before = stats.get(stat);
            int after = before + delta;
            if(stat.equals(Stats.HEALTH) && sign < 0){
                after = Math.max(after, MIN_HEALTH_AFTER_DISCARD);
            }
            stats.set(stat, after);
            if(after != before){
                lines.add(changeMessage(stat, after, after > before));
            }
        }
        return String.join("\n", lines);
    }
    private static String changeMessage(String stat, int newValue, boolean increased){
        boolean neuter = stat.equals(Stats.HEALTH);
        String possesive = neuter ? "Tvé" : "Tvá";
        String verb = increased ? (neuter ? "zvýšilo" : "zvýšila") : (neuter ? "snížilo" : "snížila");
        return possesive + " " + Stats.labelCZ(stat) + " se " + verb + " na " + newValue + ".";
    }
}
