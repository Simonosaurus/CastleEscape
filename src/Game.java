import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class Game {

    private final Scanner scanner = new Scanner(System.in);
    private Player player;
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
        player.pickUp(item);
        System.out.println("Sebral jsi: " + item + ".");
    }

    private void describeCurrentRoom() {
        Room room = player.getCurrentRoom();
        System.out.println("Nacházíš se zde: " + room.getName() + ".");
        if (!room.getDescription().isBlank()) {
            System.out.println(room.getDescription());
            System.out.print(RoomArt.draw(room));
        }
        System.out.println(room.describeItems());
    }

    private void printHelp() {
        System.out.println("Příkazy:");
        System.out.println("  N / E / S / W - pohyb na sever, východ, jih nebo západ");
        System.out.println("  I - vypíše tvůj inventář");
        System.out.println("  P - sebere předmět ležící v místnosti");
        System.out.println("  L - vypíše předměty v místnosti");
        System.out.println("  H - zobrazí tuto nápovědu");
        System.out.println("  Q - ukončí hru");
    }

    private String readLine() {
        if (!scanner.hasNextLine()) {
            running = false;
            return null;
        }
        return scanner.nextLine();
    }
}
