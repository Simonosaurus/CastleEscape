import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    private Room harbour;
    private Room hall;
    private Player player;

    @BeforeEach
    void setUp() {
        harbour = new Room(0, "Hradní přístav");
        hall = new Room(1, "Hradní hala");
        player = new Player(harbour);
    }

    @Test
    void newPlayerHasStartingStatsAndEmptyInventory() {
        assertEquals(10, player.getStats().getStrength());
        assertEquals(10, player.getStats().getHealth());
        assertTrue(player.getInventory().isEmpty());
        assertNull(player.getPreviousRoom());
    }

    @Test
    void movingRemembersPreviousRoom() {
        player.setCurrentRoom(hall);
        assertSame(hall, player.getCurrentRoom());
        assertSame(harbour, player.getPreviousRoom());
    }

    @Test
    void pickingUpItemChangesStatsAndFillsInventory() {
        Item flower = new Item("Kouzelná květina", new Stats(0, 10, 0, 0));
        String message = player.pickUp(flower);
        assertEquals(20, player.getStats().getWisdom());
        assertTrue(player.getInventory().contains(flower));
        assertEquals("Tvá moudrost se zvýšila na 20.", message);
    }

    @Test
    void pickingUpWeaponEquipsItAndAddsDamage() {
        Weapon sword = new Weapon("Meč", 10, new Stats(12, 0, 0, 0));
        player.pickUp(sword);
        assertEquals(22, player.getStats().getStrength());
        assertEquals(1, player.getEquippedWeapons().size());
        assertEquals(10, player.getWeaponDamage());
    }

    @Test
    void weaponDamageIsSumOfAllHeldWeapons() {
        player.pickUp(new Weapon("Meč", 10, new Stats(12, 0, 0, 0)));
        player.pickUp(new Weapon("Claymore", 50, new Stats(-20, 0, 0, 0)));
        assertEquals(60, player.getWeaponDamage());
    }

    @Test
    void discardingRevertsStatsAndUnequipsWeapon() {
        Weapon sword = new Weapon("Meč", 10, new Stats(12, 0, 0, 0));
        player.pickUp(sword);
        player.discard(sword);
        assertEquals(10, player.getStats().getStrength());
        assertTrue(player.getInventory().isEmpty());
        assertTrue(player.getEquippedWeapons().isEmpty());
        assertEquals(0, player.getWeaponDamage());
    }

    @Test
    void discardingHealthPotionLowersHealthAgain() {
        Item potion = new Item("Lektvar", new Stats(0, 0, 0, 30));
        player.pickUp(potion);
        assertEquals(40, player.getStats().getHealth());
        player.discard(potion);
        assertEquals(10, player.getStats().getHealth());
    }

    @Test
    void discardingHealthPotionNeverKillsPlayer() {
        Item potion = new Item("Lektvar", new Stats(0, 0, 0, 30));
        player.pickUp(potion);
        player.getStats().setHealth(5);   // hráč mezitím utrpěl zranění
        player.discard(potion);
        assertEquals(1, player.getStats().getHealth());
    }

    @Test
    void findItemIgnoresCase() {
        Item shield = new Item("Štít");
        player.pickUp(shield);
        assertSame(shield, player.findItem("štít"));
        assertNull(player.findItem("Meč"));
    }

    @Test
    void restoreItemDoesNotChangeStats() {
        player.restoreItem(new Weapon("Meč", 10, new Stats(12, 0, 0, 0)));
        assertEquals(10, player.getStats().getStrength());   // statistiky se při LOAD nastavují zvlášť
        assertEquals(1, player.getEquippedWeapons().size());
    }

    @Test
    void describeInventoryShowsItemsAndStats() {
        assertTrue(player.describeInventory().contains("Tvůj inventář je prázdný."));
        player.pickUp(new Item("Štít"));
        String text = player.describeInventory();
        assertTrue(text.contains("Štít"));
        assertTrue(text.contains("Síla: 10"));
    }
}
