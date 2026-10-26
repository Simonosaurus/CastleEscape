public class Weapon extends Item {
    private final int damage;

    public Weapon(String name, int damage) {
        this(name, damage, Stats.empty());
    }
    public Weapon(String name, int damage, Stats modifier){
        super(name, modifier);
        this.damage = damage
    }

    public int getDamage() {
        return damage;
    }

    @Override
    protected String describeModifiers(){
        String stats = super.describeModifiers();
        String dmg = "damage=" + damage;
        return stats.isEmpty() ? dmg : dmg + ", " + stats;
    }
}
