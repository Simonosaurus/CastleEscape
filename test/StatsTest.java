import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatsTest {

    @Test
    void startingStatsAreAllTen() {
        Stats stats = Stats.starting();
        for (String name : Stats.NAMES) {
            assertEquals(10, stats.get(name), name);
        }
    }

    @Test
    void emptyStatsAreAllZero() {
        Stats stats = Stats.empty();
        for (String name : Stats.NAMES) {
            assertEquals(0, stats.get(name), name);
        }
    }

    @Test
    void setAndGetWorkByName() {
        Stats stats = Stats.starting();
        stats.set(Stats.WISDOM, 25);
        assertEquals(25, stats.get(Stats.WISDOM));
        assertEquals(25, stats.getWisdom());
        assertEquals(10, stats.getStrength());
    }

    @Test
    void addReturnsNewValueAndAcceptsNegativeAmount() {
        Stats stats = Stats.starting();
        assertEquals(22, stats.add(Stats.STRENGTH, 12));
        assertEquals(2, stats.add(Stats.STRENGTH, -20));
        assertEquals(2, stats.getStrength());
    }

    @Test
    void unknownStatNameThrows() {
        Stats stats = Stats.starting();
        assertThrows(IllegalArgumentException.class, () -> stats.get("luck"));
        assertThrows(IllegalArgumentException.class, () -> stats.set("luck", 5));
        assertThrows(IllegalArgumentException.class, () -> stats.add("luck", 5));
    }

    @Test
    void isValidNameRecognisesOnlyFourStats() {
        assertTrue(Stats.isValidName("strength"));
        assertTrue(Stats.isValidName("health"));
        assertFalse(Stats.isValidName("Strength"));
        assertFalse(Stats.isValidName(""));
    }

    @Test
    void copyIsIndependentOfOriginal() {
        Stats original = Stats.starting();
        Stats copy = original.copy();
        copy.setHealth(99);
        assertEquals(10, original.getHealth());
        assertEquals(99, copy.getHealth());
    }

    @Test
    void describeNonZeroListsOnlyNonZeroStats() {
        Stats stats = new Stats(12, 0, 0, 0);
        assertEquals("strength=12", stats.describeNonZero());
        assertEquals("", Stats.empty().describeNonZero());
    }

    @Test
    void czechLabelsAreAvailable() {
        assertEquals("síla", Stats.labelCZ(Stats.STRENGTH));
        assertEquals("moudrost", Stats.labelCZ(Stats.WISDOM));
        assertThrows(IllegalArgumentException.class, () -> Stats.labelCZ("luck"));
    }
}
