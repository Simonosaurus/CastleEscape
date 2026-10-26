import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Postaví výchozí stav herního světa z CSV souborů ve složce data (oddělovač ";", první řádek je hlavička).
 *
 * Teď se načítá rooms.csv, exits.csv, items.csv (jen název předmětu) a monsters.csv.
 * Soubor conditions.csv a sloupce type/stat/value/damage v items.csv
 * se zapojí, až budou hotové třídy EntryCondition a Weapon.
 */
public class WorldLoader {

    public static final int START_ROOM_ID = 0;

    private static final String SEPARATOR = ";";

    private final Path dataDir;
    private final Map<Integer, Room> rooms = new HashMap<>();

    public WorldLoader(Path dataDir) {
        this.dataDir = dataDir;
    }

    /** Vrátí startovní místnost (ID 0). Při chybě v datech vyhodí IllegalStateException se zprávou, kde je problém. */
    public Room load() {
        rooms.clear();
        loadRooms();
        loadExits();
        loadItems();
        loadMonsters();

        Room start = rooms.get(START_ROOM_ID);
        if (start == null) {
            throw new IllegalStateException("rooms.csv neobsahuje startovní místnost s ID " + START_ROOM_ID + ".");
        }
        return start;
    }

    private void loadRooms() {
        for (Row row : readRows("rooms.csv", 5)) {
            int id = row.intAt(0);
            if (rooms.containsKey(id)) {
                throw row.error("místnost s ID " + id + " už existuje");
            }
            Room room = new Room(id, row.at(1));
            room.setDescription(row.at(2));
            room.setWinRoom(Boolean.parseBoolean(row.at(3)));
            room.setWelcomeMessage(row.at(4));
            rooms.put(id, room);
        }
    }

    private void loadExits() {
        for (Row row : readRows("exits.csv", 3)) {
            Room from = row.roomAt(0);
            String direction = row.at(1).toUpperCase();
            if (!List.of("N", "E", "S", "W").contains(direction)) {
                throw row.error("neplatný směr '" + row.at(1) + "' (povoleno N, E, S, W)");
            }
            from.addExit(direction, row.roomAt(2));
        }
    }

    private void loadItems() {
        for (Row row : readRows("items.csv", 6)) {
            try {
                Item item = Item.fromData(row.at(2), row.at(1), row.at(3), row.intOrZeroAt(4), row.intOrZeroAt(5));
                row.roomAt(0).addItem(item);
            } catch (IllegalArgumentException e){
                throw row.error(e.getMessage());
            }
        }
    }

    private void loadMonsters() {
        for (Row row : readRows("monsters.csv", 6)) {
            row.roomAt(0).addMonster(new Monster(row.at(1), row.intAt(2), row.intAt(3), row.intAt(4), row.at(5)));
        }
    }

    private List<Row> readRows(String fileName, int columns) {
        Path file = dataDir.resolve(fileName);
        List<String> lines;
        try {
            lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Nepodařilo se načíst soubor " + file + ": " + e.getMessage(), e);
        }

        List<Row> rows = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {   // řádek 0 je hlavička
            String line = lines.get(i);
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            Row row = new Row(fileName, i + 1, line.split(SEPARATOR, -1));
            if (row.cells.length != columns) {
                throw row.error("očekáváno " + columns + " sloupců, nalezeno " + row.cells.length);
            }
            rows.add(row);
        }
        return rows;
    }

    private class Row {
        final String fileName;
        final int lineNumber;
        final String[] cells;

        Row(String fileName, int lineNumber, String[] cells) {
            this.fileName = fileName;
            this.lineNumber = lineNumber;
            this.cells = cells;
        }

        String at(int index) {
            return cells[index].trim();
        }

        int intAt(int index) {
            try {
                return Integer.parseInt(at(index));
            } catch (NumberFormatException e) {
                throw error("'" + at(index) + "' není celé číslo");
            }
        }
        int intOrZeroAt(int index){
            return at(index).isEmpty() ? 0 : intAt(index);
        }

        Room roomAt(int index) {
            int id = intAt(index);
            Room room = rooms.get(id);
            if (room == null) {
                throw error("neexistuje místnost s ID " + id);
            }
            return room;
        }

        IllegalStateException error(String message) {
            return new IllegalStateException(fileName + ", řádek " + lineNumber + ": " + message);
        }
    }
}
