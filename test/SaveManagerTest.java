import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SaveManagerTest {

    @TempDir
    Path tempDir;

    private Path saveFile;
    private SaveManager saves;

    @BeforeEach
    void setUp() {
        saveFile = tempDir.resolve("savegame.txt");
        saves = new SaveManager(saveFile);
    }

    /** Malý svět 0 - 1 - 2 (oboustranné východy). V místnosti 1 je meč, v místnosti 2 krysa a dvě pavouci. */
    private Room buildWorld() {
        Room r0 = new Room(0, "Přístav");
        Room r1 = new Room(1, "Hala");
        Room r2 = new Room(2, "Sklep");
        r0.addExit("E", r1);
        r1.addExit("W", r0);
        r1.addExit("E", r2);
        r2.addExit("W", r1);
        r0.addItem(new Item("Kouzelná květina", new Stats(0, 10, 0, 0)));
        r1.addItem(new Weapon("Meč", 10, new Stats(12, 0, 0, 0)));
        r2.addMonster(new Monster("Krysa", 5, 5, 10));
        r2.addMonster(new Monster("Pavouk", 20, 8, 12));
        return r0;
    }

    private Room roomById(Room start, int id) {
        Room r = start;
        while (r.getId() != id) {
            r = r.getExit("E");
        }
        return r;
    }

    @Test
    void existsIsFalseBeforeFirstSave() {
        assertFalse(saves.exists());
    }

    @Test
    void saveCreatesFile() {
        Room start = buildWorld();
        saves.save(new Player(start));
        assertTrue(saves.exists());
    }

    @Test
    void roundTripRestoresPositionStatsAndInventory() {
        Room world = buildWorld();
        Player player = new Player(world);
        Item flower = world.findItem("Kouzelná květina");
        world.removeItem(flower);
        player.pickUp(flower);                       // moudrost 20
        player.setCurrentRoom(world.getExit("E"));   // Hala, předchozí je Přístav
        saves.save(player);

        Player loaded = saves.load(buildWorld());

        assertEquals(1, loaded.getCurrentRoom().getId());
        assertEquals(0, loaded.getPreviousRoom().getId());
        assertEquals(20, loaded.getStats().getWisdom());
        assertEquals(1, loaded.getInventory().size());
        assertEquals("Kouzelná květina", loaded.getInventory().get(0).getName());
    }

    @Test
    void roundTripRestoresItemsOnTheGround() {
        Room world = buildWorld();
        Player player = new Player(world);
        world.removeItem(world.findItem("Kouzelná květina"));   // sebráno a pryč
        Room hall = world.getExit("E");
        hall.addItem(new Item("Štít"));                         // něco přibylo na zem
        saves.save(player);

        Room freshWorld = buildWorld();
        saves.load(freshWorld);

        assertNull(freshWorld.findItem("Kouzelná květina"));
        assertNotNull(freshWorld.getExit("E").findItem("Meč"));
        assertNotNull(freshWorld.getExit("E").findItem("Štít"));
    }

    @Test
    void roundTripKeepsWeaponAsWeaponWithDamage() {
        Room world = buildWorld();
        Player player = new Player(world);
        Weapon sword = (Weapon) world.getExit("E").findItem("Meč");
        world.getExit("E").removeItem(sword);
        player.pickUp(sword);
        saves.save(player);

        Player loaded = saves.load(buildWorld());

        assertEquals(1, loaded.getEquippedWeapons().size());
        assertEquals(10, loaded.getWeaponDamage());
        assertEquals(22, loaded.getStats().getStrength());   // vlastnosti se nesmí po načtení započítat dvakrát
    }

    @Test
    void roundTripKeepsDeadMonstersDeadAndDamagedOnesDamaged() {
        Room world = buildWorld();
        Player player = new Player(world);
        Room cellar = roomById(world, 2);
        Monster rat = cellar.getMonsters().get(0);
        cellar.removeMonster(rat);                       // krysa je mrtvá
        cellar.getMonsters().get(0).takeDamage(7);       // pavouk má 13 zdraví
        saves.save(player);

        Room freshWorld = buildWorld();
        saves.load(freshWorld);

        Room freshCellar = roomById(freshWorld, 2);
        assertEquals(1, freshCellar.getMonsters().size());
        assertEquals("Pavouk", freshCellar.getMonsters().get(0).getName());
        assertEquals(13, freshCellar.getMonsters().get(0).getHealth());
    }

    @Test
    void savingTwiceGivesIdenticalFile() throws IOException {
        Player player = new Player(buildWorld());
        saves.save(player);
        String first = Files.readString(saveFile);
        saves.save(player);
        assertEquals(first, Files.readString(saveFile));
    }

    @Test
    void loadingMissingFileThrowsReadableError() {
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> saves.load(buildWorld()));
        assertTrue(e.getMessage().contains("neexistuje"));
    }

    @Test
    void emptyFileIsRejected() throws IOException {
        Files.writeString(saveFile, "");
        assertThrows(IllegalStateException.class, () -> saves.load(buildWorld()));
    }

    @Test
    void foreignFileIsRejected() throws IOException {
        Files.writeString(saveFile, "blabla\n");
        assertThrows(IllegalStateException.class, () -> saves.load(buildWorld()));
    }

    @Test
    void unknownRoomIdIsRejected() throws IOException {
        Files.writeString(saveFile, "CASTLE_ESCAPE_SAVE;1\nPLAYER_ROOM;999\n");
        assertThrows(IllegalStateException.class, () -> saves.load(buildWorld()));
    }

    @Test
    void nonNumericStatIsRejected() throws IOException {
        Files.writeString(saveFile, "CASTLE_ESCAPE_SAVE;1\nPLAYER_ROOM;1\nSTAT;strength;abc\n");
        assertThrows(IllegalStateException.class, () -> saves.load(buildWorld()));
    }

    @Test
    void missingStatsAreRejected() throws IOException {
        Files.writeString(saveFile, "CASTLE_ESCAPE_SAVE;1\nPLAYER_ROOM;1\nSTAT;strength;10\n");
        assertThrows(IllegalStateException.class, () -> saves.load(buildWorld()));
    }
}
