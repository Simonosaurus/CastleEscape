import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;

/**
 * Zapisuje celý průběh hry (výstup hry i vstupy hráče) do log.txt. Každé spuštění se připíše na konec souboru.
 *
 * Výstup se zachytává přesměrováním System.out, takže ostatní třídy se nemusí měnit.
 * Vstupy hráče se zapisují voláním input() z herní smyčky.
 */
public class GameLogger {

    public static final Path DEFAULT_FILE = Path.of("log.txt");

    private static PrintStream log;

    private GameLogger() {
    }

    /** Spustí logování. Když se soubor nedá otevřít, hra běží dál bez logu. */
    public static synchronized void start() {
        try {
            log = new PrintStream(new FileOutputStream(DEFAULT_FILE.toFile(), true), true, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Log " + DEFAULT_FILE + " se nepodařilo otevřít, hra se neloguje: " + e.getMessage());
            return;
        }
        log.println();
        log.println("=== Nová hra " + LocalDateTime.now().withNano(0) + " ===");

        PrintStream console = System.out;
        Charset charset = Charset.forName(System.getProperty("stdout.encoding", Charset.defaultCharset().name()));
        System.setOut(new PrintStream(new OutputStream() {
            private final java.io.ByteArrayOutputStream pending = new java.io.ByteArrayOutputStream();

            @Override
            public void write(int b) {
                console.write(b);
                pending.write(b);
            }

            @Override
            public void write(byte[] bytes, int off, int len) {
                console.write(bytes, off, len);
                pending.write(bytes, off, len);
            }

            @Override
            public void flush() {
                console.flush();
                synchronized (GameLogger.class) {
                    log.print(new String(pending.toByteArray(), charset));
                    log.flush();
                }
                pending.reset();
            }
        }, true, charset));
    }

    /** Zapíše řádek, který hráč zadal. */
    public static synchronized void input(String line) {
        if (log != null) {
            System.out.flush();
            log.println(line == null ? "<konec vstupu>" : line);
        }
    }
}
