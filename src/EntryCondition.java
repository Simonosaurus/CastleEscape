public class EntryCondition {
    private final String stat;
    private final int minValue;
    private final String failMessage;

    public EntryCondition(String stat, int minValue, String failMessage) {
        if(!Stats.isValidName(stat)){
            throw new IllegalArgumentException("neznámá vlastnost '" + stat + "' (povoleno strength, wisdom, agility, health)");
        }
        this.stat = stat;
        this.minValue = minValue;
        this.failMessage = failMessage;
    }

    public String getStat() {
        return stat;
    }

    public int getMinValue() {
        return minValue;
    }

    public String getFailMessage() {
        return failMessage;
    }

    public boolean isMetBy(Player player){
        return player.getStats().get(stat) >= minValue;
    }
}
