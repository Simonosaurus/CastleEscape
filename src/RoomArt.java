public class RoomArt {

    private RoomArt() {}

    // lastMove = Latest direction hraca
    public static String draw(Room room, String lastMove) {
        boolean hasN = room.getExit("N") != null;
        boolean hasE = room.getExit("E") != null;
        boolean hasS = room.getExit("S") != null;
        boolean hasW = room.getExit("W") != null;

        // Logika aby velkost chodby zmestila názov
        String name = room.getName();
        int width = Math.max(15, name.length() + 4);
        if (width % 2 == 0) {
            width++;   // odd width aby boli dvere presne v strede
        }

        // Veľkosti dverí:
        int door = 5; //Horne/spodne dvere ostatok self explanatory
        int leftWall = (width - door - 2) / 2;
        int rightWall = width - door - 2 - leftWall;

        // hrac je vždy napravo unless išiel doľava
        boolean playerOnRight = "W".equals(lastMove);

        StringBuilder sb = new StringBuilder();

        // --- TOP WALL ---
        if (hasN) { //Má North                                                                                              width
            sb.repeat(" ", leftWall + 2).append("|").repeat(" ", door).append("|\n"); //Printne cestu: |     |
            sb.append(" ┌").repeat("─", leftWall).append("┘").repeat(" ", door) // Corner + LeftTop Stena + Inside corner + door width.. atď
                    .append("└").repeat("─", rightWall).append("┐\n"); //
        } else {
            sb.append(" ┌").repeat("─", width).append("┐\n"); //Corner + Width + Corner
        }

        // --- EMPTY ROW ---
        sb.append(" |").repeat(" ", width).append("|\n");

        // --- ROW WITH THE NAME ---
        int padLeft = (width - name.length()) / 2;
        int padRight = width - name.length() - padLeft;
        sb.append(hasW ? "_|" : " |").repeat(" ", padLeft).append(name).repeat(" ", padRight)
                .append(hasE ? "|_\n" : "|\n");

        // --- ROW WITH THE SIDE DOORS AND THE PLAYER ---
        // open door = no wall there
        String leftSide = hasW ? "  " : " |";
        String rightSide = hasE ? " " : "|";
        String middle;
        if (playerOnRight) {
            middle = " ".repeat(width - 2) + "O ";
        } else {
            middle = " O" + " ".repeat(width - 2);
        }
        sb.append(leftSide).append(middle).append(rightSide).append("\n");

        // --- EMPTY ROW ---
        sb.append(hasW ? "‾|" : " |").repeat(" ", width).append(hasE ? "|‾\n" : "|\n");

        // --- BOTTOM WALL ---
        if (hasS) {
            sb.append(" └").repeat("─", leftWall).append("┐").repeat(" ", door)
                    .append("┌").repeat("─", rightWall).append("┘\n");
            sb.repeat(" ", leftWall + 2).append("|").repeat(" ", door).append("|\n");
        } else {
            sb.append(" └").repeat("─", width).append("┘\n");
        }

        return sb.toString();
    }

    // line under the picture: where can we go
    public static String describeExits(Room room) {
        String text = "Východy:";
        if (room.getExit("N") != null) {
            text += "  N (sever)";
        }
        if (room.getExit("E") != null) {
            text += "  E (východ)";
        }
        if (room.getExit("S") != null) {
            text += "  S (jih)";
        }
        if (room.getExit("W") != null) {
            text += "  W (západ)";
        }
        if (text.equals("Východy:")) {
            text += "  žádné";
        }
        return text;
    }
}