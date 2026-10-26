import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Souboj hráče s nepřáteli v místnosti.
 *
 * Použití ve hře: po vstupu hráče do nové místnosti zavolat enterRoom(player, room).
 * Vstup od hráče se čte přes dodaný Supplier (vrací null na konci vstupu).
 */
public class CombatSystem {

    public enum Result {
        /** V místnosti nejsou žádní nepřátelé. */
        NO_MONSTERS,
        /** Hráč se rozhodl nebojovat a ustoupil do předchozí místnosti. */
        RETREATED,
        /** Hráč porazil všechny nepřátele, zůstává v místnosti. */
        VICTORY,
        /** Hráč prohrál, přišel o polovinu zdraví a ustoupil do předchozí místnosti. */
        DEFEATED,
        /** Zdraví hráče kleslo na nulu, hra končí. */
        PLAYER_DIED
    }

    private final Supplier<String> input;

    public CombatSystem(Supplier<String> input) {
        this.input = input;
    }

    public Result enterRoom(Player player, Room room) {
        if (room.getMonsters().isEmpty()) {
            return Result.NO_MONSTERS;
        }

        warn(room);
        if (!askToFight()) {
            System.out.println("Raději ustupuješ zpět.");
            retreat(player);
            return Result.RETREATED;
        }

        Stats stats = player.getStats();
        int healthBefore = stats.getHealth();

        for (Monster monster : new ArrayList<>(room.getMonsters())) {
            if (!duel(player, monster)) {
                monster.resetHealth();
                return handleDefeat(player, healthBefore);
            }
            room.removeMonster(monster);
        }

        stats.setHealth(healthBefore);
        System.out.println("Porazil jsi všechny nepřátele v místnosti. Tvé zdraví se obnovilo na " + healthBefore + ".");
        return Result.VICTORY;
    }

    private void warn(Room room) {
        List<Monster> monsters = room.getMonsters();
        System.out.println("Pozor! V místnosti " + room.getName() + " číhá nebezpečí: " + String.join(", ", names(monsters)) + ".");
        for (Monster monster : monsters) {
            if (!monster.getIntro().isBlank()) {
                System.out.println(monster.getIntro());
            }
        }
    }

    private boolean askToFight() {
        while (true) {
            System.out.print("Chceš bojovat (A), nebo ustoupit (N)? ");
            String answer = input.get();
            if (answer == null) {
                return false;
            }
            switch (answer.trim().toUpperCase()) {
                case "A", "ANO" -> {
                    return true;
                }
                case "N", "NE" -> {
                    return false;
                }
                default -> System.out.println("Zadej A (bojovat) nebo N (ustoupit).");
            }
        }
    }

    /** Odehraje souboj s jedním nepřítelem. Vrací true, pokud hráč vyhrál. */
    private boolean duel(Player player, Monster monster) {
        Stats stats = player.getStats();
        int playerDamage = Math.max(1, stats.getStrength() + player.getWeaponDamage());
        int monsterDamage = Math.max(1, monster.getStrength());
        boolean playerAttacks = stats.getAgility() >= monster.getAgility();

        System.out.println();
        System.out.println("Souboj s nepřítelem: " + monster.getName() + " (zdraví " + monster.getHealth() + ")");
        System.out.println(playerAttacks
                ? "Jsi obratnější, útočíš jako první."
                : "Nepřítel je obratnější a útočí jako první.");

        while (true) {
            if (playerAttacks) {
                monster.takeDamage(playerDamage);
                System.out.println("Útočíš a způsobuješ " + playerDamage
                        + " poškození (nepříteli zbývá " + monster.getHealth() + " zdraví).");
                if (!monster.isAlive()) {
                    System.out.println("Nepřítel byl poražen: " + monster.getName() + ".");
                    return true;
                }
            } else {
                stats.setHealth(Math.max(0, stats.getHealth() - monsterDamage));
                System.out.println("Nepřítel útočí a způsobuje ti " + monsterDamage
                        + " poškození (zbývá ti " + stats.getHealth() + " zdraví).");
                if (stats.getHealth() <= 0) {
                    System.out.println("Souboj jsi prohrál.");
                    return false;
                }
            }
            playerAttacks = !playerAttacks;
        }
    }

    private Result handleDefeat(Player player, int healthBefore) {
        Stats stats = player.getStats();
        int remaining = healthBefore - (healthBefore + 1) / 2;   // přijde o polovinu zdraví z doby před bojem
        if (remaining <= 0) {
            stats.setHealth(0);
            System.out.println("Rány byly příliš těžké. Zemřel jsi.");
            return Result.PLAYER_DIED;
        }
        stats.setHealth(remaining);
        System.out.println("Jsi zraněný, tvé zdraví kleslo na " + remaining + ". Ustupuješ do předchozí místnosti.");
        retreat(player);
        return Result.DEFEATED;
    }

    private void retreat(Player player) {
        Room previous = player.getPreviousRoom();
        if (previous != null) {
            player.setCurrentRoom(previous);
        }
    }

    private static List<String> names(List<Monster> monsters) {
        List<String> names = new ArrayList<>();
        for (Monster monster : monsters) {
            names.add(monster.getName());
        }
        return names;
    }
}
