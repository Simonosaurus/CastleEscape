import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ItemTest {

    @Test
    void fromDataCreatesPlainItem() {
        Item item = Item.fromData("item", "Lektvar", "health", 30, 0);
        assertFalse(item instanceof Weapon);
        assertEquals("Lektvar", item.getName());
        assertEquals(30, item.getModifier().getHealth());
    }

    @Test
    void fromDataCreatesWeaponWithDamage() {
        Item item = Item.fromData("weapon", "Meč", "strength", 12, 10);
        assertInstanceOf(Weapon.class, item);
        assertEquals(10, ((Weapon) item).getDamage());
        assertEquals(12, item.getModifier().getStrength());
    }

    @Test
    void fromDataWithoutStatHasNoModifier() {
        Item item = Item.fromData("item", "Štít", "", 0, 0);
        assertEquals("", item.getModifier().describeNonZero());
    }

    @Test
    void fromDataRejectsUnknownStat() {
        assertThrows(IllegalArgumentException.class,
                () -> Item.fromData("item", "Kámen", "luck", 5, 0));
    }

    @Test
    void modifierGetterReturnsDefensiveCopy() {
        Item item = new Item("Květina", new Stats(0, 10, 0, 0));
        item.getModifier().setWisdom(999);
        assertEquals(10, item.getModifier().getWisdom());
    }

    @Test
    void toStringShowsModifiers() {
        assertEquals("Štít", new Item("Štít").toString());
        assertEquals("Květina{wisdom=10}", new Item("Květina", new Stats(0, 10, 0, 0)).toString());
        assertEquals("Meč{damage=10, strength=12}", new Weapon("Meč", 10, new Stats(12, 0, 0, 0)).toString());
    }
}
