/**
 * autor Willian J. Dos Santos
 * data 24/04/2022
 */
import server.STTP1;

public class App {
    public static void main(String[] args) {

        String dado = "hello i love you let me jump in your game hello i love you let me jump in your " +
                "game hello i love you let me jump in your game hello i love you let me jump in your game hello i love you " +
                "let me jump in your game hello i love you let me jump in your game hello i love you let me jump in your game " +
                "hello i love you let me jump in your game hello i love you let me jump in your game ";

        STTP1 serverRefactoring = new STTP1(8189, 80, 50, dado,15);
        serverRefactoring.startServer();

    }
}
