import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class WorldLoaderTest {

    private static final Path DATA = Path.of("data");   // Maven spouští testy z kořene projektu

    @TempDir
    Path emptyDir;

    @Test
    void loadsStartingRoomWithIdZero() {
        Room start = new WorldLoader(DATA).load();
        assertEquals(0, start.getId());
        assertEquals("Hradní přístav", start.getName());
    }

    @Test
    void roomsAreConnectedBothWays() {
        Room harbour = new WorldLoader(DATA).load();
        Room hall = harbour.getExit("E");
        assertNotNull(hall);
        assertEquals("Hradní hala", hall.getName());
        assertSame(harbour, hall.getExit("W"));
    }

    @Test
    void hallRequiresWisdomFifteen() {
        Room hall = new WorldLoader(DATA).load().getExit("E");
        EntryCondition condition = hall.getEntryCondition();
        assertNotNull(condition);
        assertEquals(Stats.WISDOM, condition.getStat());
        assertEquals(15, condition.getMinValue());
    }

    @Test
    void harbourHasFlowerAndNoCondition() {
        Room harbour = new WorldLoader(DATA).load();
        assertNotNull(harbour.findItem("Kouzelná květina"));
        assertNull(harbour.getEntryCondition());
    }

    @Test
    void worldCanBeCompletedFromStartToWinRoom() {
        // Přístav -> hala -> L1 -> L2 -> L3 -> křižovatka -> soudní síň -> zatopená místnost -> Villedor
        Room room = new WorldLoader(DATA).load();
        for (String direction : new String[]{"E", "N", "E", "E", "S", "E", "E", "E"}) {
            room = room.getExit(direction);
            assertNotNull(room, "chybí východ " + direction);
        }
        assertTrue(room.isWinRoom());
        assertFalse(room.getWelcomeMessage().isBlank());
    }

    @Test
    void cellarHasFourRats() {
        Room room = new WorldLoader(DATA).load();
        for (String direction : new String[]{"E", "S", "E"}) {
            room = room.getExit(direction);
        }
        assertEquals(4, room.getMonsters().size());
    }

    @Test
    void missingDataFilesGiveReadableError() {
        WorldLoader loader = new WorldLoader(emptyDir);
        IllegalStateException e = assertThrows(IllegalStateException.class, loader::load);
        assertNotNull(e.getMessage());
    }

    @Test
    void eachLoadBuildsFreshWorld() {
        Room first = new WorldLoader(DATA).load();
        first.removeItem(first.findItem("Kouzelná květina"));
        Room second = new WorldLoader(DATA).load();
        assertNotNull(second.findItem("Kouzelná květina"));
    }
}
