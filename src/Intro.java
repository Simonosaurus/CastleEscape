import java.util.function.Supplier;

/**
 * Úvodní obrazovka hry: název, příběh a cíl. Čeká na Enter, než hra začne.
 */
public class Intro {

    private static final String TITLE = """
            ==============================
                    CASTLE ESCAPE
            ==============================
            """;

    private static final String STORY = """
            Neznámý hrdino, probouzíš se na molu hradního přístavu. Za zády ti šplouchá černá voda,
            před tebou se tyčí temný starodávný hrad plný nestvůr, zamčených dveří a tajemství.

            Na druhé straně hradu leží nebeská vesnice Villedor, které říkáš domov.
            Její hlavní brána je ale zavřená a jediná cesta k ní vede skrz hrad.

            Překonej nástrahy hradu, poraz nepřátele, sbírej předměty, které tě posílí,
            a najdi dost síly, abys otevřel hlavní bránu.

            Pozor na zdraví. Pokud klesne na nulu, tvé dobrodružství skončí.
            """;

    private Intro() {
    }

    public static void show(Supplier<String> input) {
        System.out.println(TITLE);
        System.out.println(STORY);
        System.out.print("Stiskni Enter a vyraz do hradu...");
        input.get();
        System.out.println();
        System.out.println();
    }
}
