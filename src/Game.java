import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class Game {

    private final Scanner scanner = new Scanner(System.in);
    private Player player;
    private String lastMove;   // smer posleneho pohybu (Podla toho sa vykresli hrac)
    private boolean running = true;

    public void run() {
        Room start;
        try {
            start = loadWorld();
        } catch (IllegalStateException e) {
            System.out.println("Hru se nepodařilo spustit: " + e.getMessage());
            return;
        }
        player = new Player(start);

        System.out.println("Když dorazíš k molu, rozhlížíš se po hradním přístavu a hledáš vstup do hradu. "
                + "Na druhé straně leží nebeská vesnice, které říkáš domov.");
        System.out.println();
        printHelp();
        System.out.println();
        describeCurrentRoom();

        while (running) {
            System.out.print("\n> ");
            String input = readLine();
            if (input == null) {
                break;
            }
            handleCommand(input.trim());
        }
    }


    private Room loadWorld() {
        Path dataDir = Path.of("data");
        if (!Files.isDirectory(dataDir)) {
            dataDir = Path.of("..", "data");   // spuštění ze složky src
        }
        return new WorldLoader(dataDir).load();
    }


    private void handleCommand(String input) {
        switch (input.toUpperCase()) {
            case "N", "E", "S", "W" -> move(input.toUpperCase());
            case "I" -> System.out.println(player.describeInventory());
            case "P" -> pickUpItem();
            case "L" -> System.out.println(player.getCurrentRoom().describeItems());
            case "H" -> printHelp();
            case "Q" -> {
                System.out.println("Ukončuješ hru. Ahoj!");
                running = false;
            }
            default -> System.out.println("Neplatný vstup. Zadej N, E, S, W, I, P, L, H nebo Q.");
        }
    }

    private void move(String direction) {
        Room current = player.getCurrentRoom();
        Room target = current.getExit(direction);

        if (target == null) {
            System.out.println("Tímto směrem se z místnosti " + current.getName() + " jít nedá.");
            return;
        }

        player.setCurrentRoom(target);
        lastMove = direction;
        describeCurrentRoom();
    }

    private void pickUpItem() {
        Room room = player.getCurrentRoom();
        if (room.getItems().isEmpty()) {
            System.out.println("V místnosti nic neleží.");
            return;
        }
        System.out.println(room.describeItems());
        System.out.print("Zadej přesný název předmětu, který chceš sebrat: ");
        String name = readLine();
        if (name == null) {
            return;
        }
        Item item = room.findItem(name.trim());
        if (item == null) {
            System.out.println("Takový předmět tu neleží.");
            return;
        }
        room.removeItem(item);
        String statChange = player.pickUp(item);
        System.out.println("Sebral jsi: " + item.getName() + ".");
        if(!statChange.isEmpty()){
            System.out.println(statChange);
        }
        System.out.println(room.describeItems());
    }
    private void dropItem() {
        if (player.getInventory().isEmpty()) {
            System.out.println("Nemáš u sebe nic, co by šlo zahodit.");
            return;
        }
        System.out.println(player.describeInventory());
        System.out.print("Zadej přesný název předmětu, který chceš zahodit: ");
        String name = readLine();
        if (name == null) {
            return;
        }
        Item item = player.findItem(name.trim());
        if (item == null) {
            System.out.println("Takový předmět u sebe nemáš.");
            return;
        }
        String statChange = player.discard(item);
        player.getCurrentRoom().addItem(item);
        System.out.println("Zahodil jsi: " + item.getName() + ".");
        if (!statChange.isEmpty()) {
            System.out.println(statChange);
        }
    }

    private void saveGame() {
        SaveManager saves = new SaveManager();
        try {
            saves.save(player);
            System.out.println("Hra byla uložena do souboru " + saves.getFile() + ".");
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
        }
    }

    private void loadGame() {
        SaveManager saves = new SaveManager();
        try {
            Player loaded = saves.load(loadWorld());
            player = loaded;
            lastMove = null;
            System.out.println("Hra byla načtena ze souboru " + saves.getFile() + ".");
            describeCurrentRoom();
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
        }
    }


    private void describeCurrentRoom() {
        Room room = player.getCurrentRoom();
        System.out.println("Nacházíš se zde: " + room.getName() + ".");
        if (!room.getDescription().isBlank()) {
            System.out.println(room.getDescription());
        }
        System.out.print(RoomArt.draw(room, lastMove));
        System.out.println(room.describeItems());
        System.out.println(RoomArt.describeExits(room));
    }

    private void printHelp() {
        Room room = player.getCurrentRoom();
        System.out.println("Příkazy:");
        System.out.println("  N / E / S / W - pohyb na sever, východ, jih nebo západ");
        System.out.println("  I - vypíše tvůj inventář");
        System.out.println("  P - sebere předmět ležící v místnosti");
        System.out.println("  L - vypíše předměty v místnosti");
        System.out.println("  H - zobrazí tuto nápovědu");
        System.out.println("  Q - ukončí hru");
        System.out.println("V obrázku místnosti jsi ty (O).");
        System.out.print(RoomArt.draw(room, lastMove));
        System.out.println(RoomArt.describeExits(room));
    }

    private String readLine() {
        if (!scanner.hasNextLine()) {
            running = false;
            return null;
        }
        return scanner.nextLine();
    }
}