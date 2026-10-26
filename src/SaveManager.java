import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class SaveManager {
    public static final Path DEFAULT_FILE = Path.of("savegame.txt");
    private static final String HEADER = "CASTLE_ESCAPE_SAVE";

    private static final int VERSION = 1;
    private static final String SEP = ";";
    private final Path file;

    public SaveManager(){
        this(DEFAULT_FILE);
    }
    public SaveManager(Path file) {
        this.file = file;
    }
    public Path getFile(){
        return file;
    }
    public boolean exists(){
        return Files.isRegularFile(file);
    }

    public void save(Player player){
        List<String> lines = new ArrayList<>();
        lines.add(HEADER + SEP + VERSION);
        lines.add("PLAYER_ROOM" + SEP + player.getCurrentRoom().getId());
        Room previous = player.getPreviousRoom();
        lines.add("PREVIOUS_ROOM" + SEP + (previous == null ? -1 : previous.getId()));

        for (String stat : Stats.NAMES){
            lines.add("STAT" + SEP + stat + SEP + player.getStats().get(stat));
        }
        for (Item item : player.getInventory()){
            lines.add("INVENTORY" + SEP + encode(item));
        }
        for(Room room : indexRooms(player.getCurrentRoom()).values()){
            for(Item item : room.getItems()){
                lines.add("GROUND" + SEP + room.getId() + SEP + encode(item));
            }
        }
        Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
        try{
            Files.write(tmp, lines, StandardCharsets.UTF_8);
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Hru se nepodařilo uložit do souboru " + file + ": " + e.getMessage(), e);
        }
    }
    public Player load(Room freshStart){
        if(!exists()){
            throw new IllegalStateException("Soubor s uloženou hrou (" + file + ") neexistuje. Nejdřív hru ulož příkazem SAVE.");
        }
        List<String> lines;
        try{
            lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e){
            throw new IllegalStateException("Uloženou hru se nepodařilo načíst ze souboru " + file + ": " + e.getMessage(), e);
        }
        Map<Integer, Room> rooms = indexRooms(freshStart);

        Integer playerRoomId = null;
        Integer previousRoomId = null;
        Map<String, Integer> stats = new HashMap<>();
        List<Item> inventory = new ArrayList<>();
        Map<Integer, List<Item>> ground = new LinkedHashMap<>();

        boolean headerSeen = false;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int lineNo = i + 1;
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            String[] f = line.split(SEP, -1);

            if (!headerSeen) {
                if (!f[0].equals(HEADER)) {
                    throw error(lineNo, "toto není uložená hra Castle Escape");
                }
                if (f.length != 2 || intAt(f, 1, lineNo) != VERSION) {
                    throw error(lineNo, "nepodporovaná verze uložené hry");
                }
                headerSeen = true;
                continue;
            }

            switch (f[0]) {
                case "PLAYER_ROOM" -> {
                    expectFields(f, 2, lineNo);
                    playerRoomId = roomIdAt(f, 1, lineNo, rooms, false);
                }
                case "PREVIOUS_ROOM" -> {
                    expectFields(f, 2, lineNo);
                    previousRoomId = roomIdAt(f, 1, lineNo, rooms, true);
                }
                case "STAT" -> {
                    expectFields(f, 3, lineNo);
                    if (!Stats.isValidName(f[1])) {
                        throw error(lineNo, "neznámá vlastnost '" + f[1] + "'");
                    }
                    stats.put(f[1], intAt(f, 2, lineNo));
                }
                case "INVENTORY" -> inventory.add(decode(f, 1, lineNo));
                case "GROUND" -> {
                    int roomId = roomIdAt(f, 1, lineNo, rooms, false);
                    ground.computeIfAbsent(roomId, k -> new ArrayList<>()).add(decode(f, 2, lineNo));
                }
                case "MONSTER" -> {
                    // TODO (Marek): viz popis třídy. Zatím se nepřátelé neukládají a řádek se přeskočí.
                }
                default -> throw error(lineNo, "neznámý záznam '" + f[0] + "'");
            }
        }

        if (!headerSeen) {
            throw new IllegalStateException(file + ": soubor je prázdný.");
        }
        if (playerRoomId == null) {
            throw new IllegalStateException(file + ": chybí záznam PLAYER_ROOM.");
        }
        for (String stat : Stats.NAMES) {
            if (!stats.containsKey(stat)) {
                throw new IllegalStateException(file + ": chybí vlastnost '" + stat + "'.");
            }
        }

        for (Room room : rooms.values()) {
            room.getItems().clear();
        }
        for (Map.Entry<Integer, List<Item>> entry : ground.entrySet()) {
            Room room = rooms.get(entry.getKey());
            for (Item item : entry.getValue()) {
                room.addItem(item);
            }
        }

        Player player = new Player(rooms.get(playerRoomId));
        if (previousRoomId != null && previousRoomId >= 0) {
            player.setPreviousRoom(rooms.get(previousRoomId));
        }
        for (String stat : Stats.NAMES) {
            player.getStats().set(stat, stats.get(stat));
        }
        for (Item item : inventory) {
            player.restoreItem(item);
        }
        return player;

    }
    private static Map<Integer, Room> indexRooms(Room from){
        Map<Integer, Room> found = new HashMap<>();
        Deque<Room> queue = new ArrayDeque<>();
        found.put(from.getId(), from);
        queue.add(from);
        while(!queue.isEmpty()){
            Room room = queue.poll();
            for(String direction : List.of("N", "S", "E", "W")){
                Room next = room.getExit(direction);
                if (next != null && found.putIfAbsent(next.getId(), next) == null){
                    queue.add(next);
                }
            }
        }
        Map<Integer, Room> sorted = new LinkedHashMap<>();
        found.entrySet().stream().sorted(Map.Entry.comparingByKey(Comparator.naturalOrder())).forEach(e -> sorted.put(e.getKey(), e.getValue()));
        return sorted;
    }
    private static String encode(Item item){
        Stats m = item.getModifier();
        String type = item instanceof Weapon ? "WEAPON" : "ITEM";
        int damage = item instanceof Weapon weapon ? weapon.getDamage() : 0;
        return type + SEP + item.getName() + SEP + damage + SEP + m.getStrength() + SEP + m.getWisdom() + SEP + m.getAgility() + SEP + m.getHealth();
    }
    private static Item decode(String[] f, int start, int lineNo){
        expectFields(f, start + 7, lineNo);
        String type = f[start];
        String name = f[start + 1];
        if(name.isBlank()){
            throw error(lineNo, "předmět nemá název");
        }
        int damage = intAt(f, start + 2, lineNo);
        Stats modifier = new Stats(intAt(f, start + 3, lineNo), intAt(f, start + 4, lineNo), intAt(f, start + 5, lineNo), intAt(f, start + 6, lineNo));

        return switch(type){
            case "WEAPON" -> new Weapon(name, damage, modifier);
            case "ITEM" -> new Item(name, modifier);
            default -> throw error(lineNo, "neznámý typ předmětu '" + type + "'");
        };
    }
    private static void expectFields(String[] f, int count, int lineNo){
        if(f.length != count){
            throw error(lineNo, "očekáváno " + count + " polí, nalezeno " + f.length);
        }
    }
    private static int intAt(String[] f, int index, int lineNo){
        try{
            return Integer.parseInt(f[index].trim());
        } catch (NumberFormatException e){
            throw error(lineNo, "'" + f[index] + "' není celé číslo");
        }
    }
    private static int roomIdAt(String[] f, int index, int lineNo, Map<Integer, Room> rooms, boolean allowNone){
        int id = intAt(f, index, lineNo);
        if(allowNone && id == -1){
            return -1;
        }
        if(!rooms.containsKey(id)){
            throw error(lineNo, "místnost s ID " + id + " ve světe neexistuje.");
        }
        return id;
    }
    private static IllegalStateException error(int lineNo, String message){
        return new IllegalStateException("Uložená hra je poškozená, řádek " + lineNo + ": " + message);
    }
}
