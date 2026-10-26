/**
 * Nepřítel v jedné místnosti. S nepřáteli se nedá mluvit, jen bojovat.
 */
public class Monster {

    private final String name;
    private final int maxHealth;
    private final int strength;
    private final int agility;
    private final String intro;   // hláška při vstupu do místnosti (u bossů), jinak prázdná
    private int health;

    public Monster(String name, int health, int strength, int agility) {
        this(name, health, strength, agility, "");
    }

    public Monster(String name, int health, int strength, int agility, String intro) {
        this.name = name;
        this.maxHealth = health;
        this.health = health;
        this.strength = strength;
        this.agility = agility;
        this.intro = intro;
    }

    public String getName() {
        return name;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getStrength() {
        return strength;
    }

    public int getAgility() {
        return agility;
    }

    public String getIntro() {
        return intro;
    }

    public boolean isAlive() {
        return health > 0;
    }

    public void takeDamage(int damage) {
        health = Math.max(0, health - damage);
    }

    public void resetHealth() {
        health = maxHealth;
    }

    @Override
    public String toString() {
        return name;
    }
}
