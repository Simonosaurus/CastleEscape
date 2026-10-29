import java.util.Locale;

public class Item {

    private final String name;
    private final Stats modifier;

    public Item(String name) {
        this(name, Stats.empty());
    }

    public Item(String name, Stats modifier) {
        this.name = name;
        this.modifier = modifier.copy();
    }

    public static Item fromData(String type, String name, String stat, int value, int damage){
        Stats modifier = Stats.empty();
        if(stat != null && !stat.isBlank()){
            String key = stat.trim().toLowerCase();
            if(!Stats.isValidName(key)){
                throw new IllegalArgumentException("neznámá vlastnost '" + stat + "'(povoleno: strength, wisdom, agility, health)");
            }
            modifier.set(key, value);
        }
        String normalizedType = type == null ? "" : type.trim().toLowerCase();
        boolean weapon = normalizedType.equals("weapon") || normalizedType.equals("zbraň") || normalizedType.equals("zbran") || damage > 0;
        return weapon ? new Weapon(name, damage, modifier) : new Item(name, modifier);
    }

    public String getName() {
        return name;
    }

    public Stats getModifier() {
        return modifier.copy();
    }
    protected String describeModifiers(){
        return modifier.describeNonZero();
    }

    @Override
    public String toString() {
        String text = describeModifiers();
        return text.isEmpty() ? name : name + "{" + text + "}";
    }
}
