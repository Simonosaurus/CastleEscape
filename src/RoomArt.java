public class RoomArt {
    private static final int INNER = 15;
    private static final int DOOR = 5;
    private RoomArt(){};

    public static String draw(Room room){
        boolean n = room.getExit("N") != null;
        boolean s = room.getExit("S") != null;
        boolean e = room.getExit("E") != null;
        boolean w = room.getExit("W") != null;

        StringBuilder sb = new StringBuilder();
        sb.append(horizontalWall(n)).append('\n');
        sb.append(row("",true, true)).append('\n');
        sb.append(row(room.getName(), true, true)).append('\n');
        sb.append(row("", !w, !e)).append('\n');
        sb.append(row("", true, true)).append('\n');
        sb.append(horizontalWall(s));

        return sb.toString();
    }

    public static String horizontalWall(boolean hasDoor){
        StringBuilder sb = new StringBuilder("+");
        if(hasDoor){
            int side = (INNER - DOOR) / 2;
            sb.append("-".repeat(side)).repeat(" ", DOOR).append("-".repeat(INNER - side - DOOR));
        } else {
            sb.append("-".repeat(INNER));
        }
        return sb.append("+").toString();
    }

    public static String row(String text, boolean leftWall, boolean rightWall){
        if(text.length() > INNER){
            text = text.substring(0, INNER);
        }
        int padLeft = (INNER - text.length()) / 2;
        int padRight = INNER - text.length() - padLeft;

        return (leftWall ? "|" : " ") + " ".repeat(padLeft) + text + " ".repeat(padRight) + (rightWall ? "|" : " ");
    }
}
