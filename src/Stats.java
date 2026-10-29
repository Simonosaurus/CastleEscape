import java.util.List;

public class Stats {
    public static final String STRENGTH = "strength";
    public static final String WISDOM = "wisdom";
    public static final String AGILITY = "agility";
    public static final String HEALTH = "health";

    public static final List<String> NAMES = List.of(STRENGTH, WISDOM, AGILITY, HEALTH);
    public static final int START_VALUE = 10;

    private int strength;
    private int wisdom;
    private int agility;
    private int health;

    public Stats(int strength, int wisdom, int agility, int health) {
        this.strength = strength;
        this.wisdom = wisdom;
        this.agility = agility;
        this.health = health;
    }

    public static Stats starting(){
        return new Stats(START_VALUE, START_VALUE, START_VALUE, START_VALUE);
    }
    public static Stats empty(){
        return new Stats(0,0,0,0);
    }
    public static boolean isValidName(String stat){
        return NAMES.contains(stat);
    }

    public static String labelCZ(String stat){
        return switch (stat){
            case STRENGTH -> "síla";
            case WISDOM -> "moudrost";
            case AGILITY -> "obratnost";
            case HEALTH -> "zdraví";
            default -> throw unknown(stat);
        };
    }
    public int get(String stat){
        return switch (stat){
            case STRENGTH -> strength;
            case WISDOM -> wisdom;
            case AGILITY -> agility;
            case HEALTH -> health;
            default -> throw unknown(stat);
        };
    }
    public int set(String stat, int value){
        return switch (stat){
            case STRENGTH -> strength = value;
            case WISDOM -> wisdom = value;
            case AGILITY -> agility = value;
            case HEALTH -> health = value;
            default -> throw unknown(stat);
        };
    }

    public int add(String stat, int amount){
        int updated = get(stat) + amount;
        set(stat, updated);
        return updated;
    }

    public int getStrength() {
        return strength;
    }
    public void setStrength(int strength) {
        this.strength = strength;
    }
    public int getWisdom() {
        return wisdom;
    }
    public void setWisdom(int wisdom) {
        this.wisdom = wisdom;
    }
    public int getAgility() {
        return agility;
    }
    public void setAgility(int agility) {
        this.agility = agility;
    }
    public int getHealth() {
        return health;
    }
    public void setHealth(int health) {
        this.health = health;
    }

    public Stats copy(){
        return new Stats(strength,wisdom,agility,health);
    }

    public String describeNonZero(){
        StringBuilder sb = new StringBuilder();
        for (String stat : NAMES){
            int value = get(stat);
            if(value != 0){
                if(!sb.isEmpty()){
                    sb.append(", ");
                }
                sb.append(stat).append("=").append(value);
            }
        }
        return sb.toString();
    }

    @Override
    public String toString(){
        return "strength=" + strength + ", wisdom=" + wisdom + ", agility=" + agility + ", health=" + health;
    }

    private static IllegalArgumentException unknown(String stat){
        return new IllegalArgumentException("neznámá vlastnost '" + stat + "'(povoleno: strength, wisdom, agility, health)");
    }
}
