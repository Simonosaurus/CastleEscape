import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CombatSystemTest {

    private Room corridor;
    private Room lair;
    private Player player;

    @BeforeEach
    void setUp() {
        corridor = new Room(1, "Chodba");
        lair = new Room(2, "Doupě");
        player = new Player(corridor);
        player.setCurrentRoom(lair);   // předchozí místnost je chodba
    }

    /** Souboj, kde hráč odpovídá postupně zadanými odpověďmi (po jejich vyčerpání vrací null). */
    private CombatSystem combatWithAnswers(String... answers) {
        Deque<String> queue = new ArrayDeque<>(List.of(answers));
        return new CombatSystem(queue::poll);
    }

    @Test
    void roomWithoutMonstersMeansNoFight() {
        assertEquals(CombatSystem.Result.NO_MONSTERS, combatWithAnswers().enterRoom(player, lair));
        assertSame(lair, player.getCurrentRoom());
    }

    @Test
    void retreatingMovesPlayerBackAndKeepsMonster() {
        lair.addMonster(new Monster("Krysa", 5, 5, 5));
        CombatSystem.Result result = combatWithAnswers("N").enterRoom(player, lair);
        assertEquals(CombatSystem.Result.RETREATED, result);
        assertSame(corridor, player.getCurrentRoom());
        assertEquals(1, lair.getMonsters().size());
    }

    @Test
    void noAnswerCountsAsRetreat() {
        lair.addMonster(new Monster("Krysa", 5, 5, 5));
        assertEquals(CombatSystem.Result.RETREATED, combatWithAnswers().enterRoom(player, lair));
    }

    @Test
    void invalidAnswerIsAskedAgain() {
        lair.addMonster(new Monster("Krysa", 5, 5, 5));
        CombatSystem.Result result = combatWithAnswers("možná", "A").enterRoom(player, lair);
        assertEquals(CombatSystem.Result.VICTORY, result);
    }

    @Test
    void winningRemovesMonstersAndRestoresHealthFromBeforeTheFight() {
        // První krysa je obratnější a hráči ubere 3 zdraví, než ji hráč zabije. Druhá útočí až po hráči.
        lair.addMonster(new Monster("Krysa", 5, 3, 20));
        lair.addMonster(new Monster("Tlustá krysa", 5, 5, 3));
        CombatSystem.Result result = combatWithAnswers("A").enterRoom(player, lair);
        assertEquals(CombatSystem.Result.VICTORY, result);
        assertTrue(lair.getMonsters().isEmpty());
        assertEquals(10, player.getStats().getHealth());
        assertSame(lair, player.getCurrentRoom());
    }

    @Test
    void losingMovesPlayerBackAndHalvesHealth() {
        player.getStats().setStrength(1);
        Monster dragon = new Monster("Drak", 100, 12, 5);
        lair.addMonster(dragon);
        CombatSystem.Result result = combatWithAnswers("A").enterRoom(player, lair);
        assertEquals(CombatSystem.Result.DEFEATED, result);
        assertSame(corridor, player.getCurrentRoom());
        assertEquals(5, player.getStats().getHealth());          // 10 - polovina z 10
        assertEquals(100, dragon.getHealth());                   // přeživší nepřítel se vyléčí
        assertEquals(1, lair.getMonsters().size());
    }

    @Test
    void losingWithOneHealthMeansDeath() {
        player.getStats().setHealth(1);
        player.getStats().setStrength(1);
        lair.addMonster(new Monster("Drak", 100, 12, 5));
        CombatSystem.Result result = combatWithAnswers("A").enterRoom(player, lair);
        assertEquals(CombatSystem.Result.PLAYER_DIED, result);
        assertEquals(0, player.getStats().getHealth());
    }

    @Test
    void moreAgileMonsterAttacksFirst() {
        // Hráč by krysu zabil jednou ranou, ale obratnější krysa ho zabije dřív.
        player.getStats().setHealth(5);
        lair.addMonster(new Monster("Krysa", 5, 5, 20));
        assertEquals(CombatSystem.Result.DEFEATED, combatWithAnswers("A").enterRoom(player, lair));
    }

    @Test
    void moreAgilePlayerAttacksFirst() {
        // Stejná situace, ale hráč je obratnější a vyhraje.
        player.getStats().setHealth(5);
        lair.addMonster(new Monster("Krysa", 5, 5, 3));
        assertEquals(CombatSystem.Result.VICTORY, combatWithAnswers("A").enterRoom(player, lair));
        assertEquals(5, player.getStats().getHealth());
    }

    @Test
    void weaponsIncreaseDamage() {
        player.getStats().setStrength(1);   // bez zbraně by hráč s 1 poškozením nevyhrál
        player.pickUp(new Weapon("Claymore", 50));
        lair.addMonster(new Monster("Pavouk", 20, 8, 12));
        // Pavouk je obratnější a ubere 8 ze 10 zdraví; hráč pak zabije pavouka jednou ranou za 51.
        assertEquals(CombatSystem.Result.VICTORY, combatWithAnswers("A").enterRoom(player, lair));
    }
}
