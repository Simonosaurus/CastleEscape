import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EntryConditionTest {

    private Player playerWithWisdom(int wisdom) {
        Player player = new Player(new Room(0, "Přístav"));
        player.getStats().setWisdom(wisdom);
        return player;
    }

    @Test
    void conditionIsNotMetBelowMinimum() {
        EntryCondition condition = new EntryCondition(Stats.WISDOM, 15, "Nevíš, jak se dostat do hradu.");
        assertFalse(condition.isMetBy(playerWithWisdom(14)));
    }

    @Test
    void conditionIsMetExactlyAtMinimumAndAbove() {
        EntryCondition condition = new EntryCondition(Stats.WISDOM, 15, "zpráva");
        assertTrue(condition.isMetBy(playerWithWisdom(15)));
        assertTrue(condition.isMetBy(playerWithWisdom(40)));
    }

    @Test
    void conditionChecksTheRightStat() {
        EntryCondition condition = new EntryCondition(Stats.STRENGTH, 30, "zpráva");
        Player player = playerWithWisdom(100);   // moudrost nepomůže, síla je 10
        assertFalse(condition.isMetBy(player));
    }

    @Test
    void gettersReturnConstructorValues() {
        EntryCondition condition = new EntryCondition(Stats.STRENGTH, 30, "Nejsi dost silný.");
        assertEquals(Stats.STRENGTH, condition.getStat());
        assertEquals(30, condition.getMinValue());
        assertEquals("Nejsi dost silný.", condition.getFailMessage());
    }

    @Test
    void unknownStatIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new EntryCondition("luck", 5, "zpráva"));
    }
}
