/**
 * Vstupní bod hry Castle Escape (zkrácená verze).
 * Spuštění: javac *.java && java Main
 */
public class Main {
    public static void main(String[] args) {
        GameLogger.start();
        new Game().run();
    }
}