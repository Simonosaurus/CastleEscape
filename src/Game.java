import java.util.Scanner;

public class Game {

    private final Scanner scanner = new Scanner(System.in);
    private Player player;
    private boolean running = true;

    public void run() {
        Room start = buildWorld();
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


    private Room buildWorld() {
        Room harbour = new Room(0, "Hradní přístav");
        Room lobby = new Room(1, "Hradní hala");
        Room l1 = new Room(2, "L1");
        Room l2 = new Room(3, "L2");
        Room r1 = new Room(8, "R1");
        Room r2 = new Room(9, "R2");
        Room villedor = new Room(11, "Villedor");

        harbour.addExit("E", lobby);

        lobby.addExit("N", l1);
        lobby.addExit("W", harbour);
        lobby.addExit("S", r1);

        l1.addExit("E", l2);
        l1.addExit("S", lobby);

        l2.addExit("W", l1);

        r1.addExit("N", lobby);
        r1.addExit("E", r2);

        r2.addExit("W", r1);
        r2.addExit("E", villedor);

        villedor.addExit("W", r2);

        harbour.addItem(new Item("Kouzelná květina"));
        lobby.addItem(new Item("Meč"));
        l2.addItem(new Item("Lektvar"));
        r2.addItem(new Item("Lektvar síly"));

        return harbour;
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
