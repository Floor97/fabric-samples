package applications.operator;

import datatypes.aggregationprocess.AggregationProcess;
import org.hyperledger.fabric.gateway.ContractException;

import java.io.IOException;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class ParticipantTransaction {
    private static final Scanner scan = new Scanner(System.in);

    /**
     * When submitting a transaction that will be automated, this class is used to manage rejection from
     * collision, by repeating the transaction a number of times.
     *
     * @param transaction the repeated transaction.
     * @param args        the arguments of the transaction.
     * @param id          the id of the asset the transaction references.
     * @return the response of the transaction.
     * @throws InterruptedException when the thread is interrupted when sleep is done.
     */
    public static byte[] repeat(org.hyperledger.fabric.gateway.Transaction transaction, String[] args, String id) throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            try {
                return transaction.submit(args);
            } catch (ContractException | TimeoutException | InterruptedException e) {
                System.out.println("Failed to commit transaction, trying again..." + i);
                int timeout = new Random().nextInt(10) * 200 + i * 1000;
                System.out.println("Waiting, id time: " + id + " " + timeout);
                Thread.sleep(timeout);
            }
        }
        throw new RuntimeException("Failed to commit transaction");
    }

    /**
     * Helper method that deserializes the response of a transaction and prints it.
     *
     * @param response the response of a aggregation process contract transaction.
     */
    public static void printResponse(byte[] response) throws IOException {
        AggregationProcess serAggregationProcess = AggregationProcess.deserialize(response);
        System.out.println("Response: " + serAggregationProcess);
    }

    /**
     * Helper method that prints the message and sends back the next input on System.in.
     *
     * @param message the message that will be printed.
     * @return the next input on System.in.
     */
    public static String scanNextLine(String message) {
        System.out.print(message);
        return scan.next();
    }
}
