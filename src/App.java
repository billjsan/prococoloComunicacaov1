/**
 * autor Willian J. Dos Santos
 * data 24/04/2022
 */
import server.STTP1;

public class App {
    public static void main(String[] args) {

        String dataToTransfer = "Riders on the storm " +
                "Into this house, we're born " +
                "Into this world, we're thrown " +
                "Like a dog without a bone " +
                "An actor out on loan " +
                "Riders on the storm " +
                "There's a killer on the road " +
                "His brain is squirmin' like a toad " +
                "Take a long holiday " +
                "Let your children play " +
                "If you give this man a ride " +
                "Sweet family will die " +
                "Killer on the road, yeah";

        STTP1 serverRefactoring = new STTP1(8189, 80, 70, dataToTransfer,15);
        serverRefactoring.startServer();
    }
}
