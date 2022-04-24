
import server.Server;
import server.Serv;

public class App {
    public static void main(String[] args) {


        Serv serverRefactoring = new Serv(8189, 85, 25);
        serverRefactoring.startServer();

    }
}
