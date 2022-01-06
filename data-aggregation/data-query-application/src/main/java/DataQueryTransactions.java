import com.n1analytics.paillier.PaillierPublicKey;
import dataquery.DataQuery;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractException;
import shared.Pair;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class DataQueryTransactions {
    private static Scanner scan = new Scanner(System.in);

    public static void start(Contract contract) throws ContractException, InterruptedException, TimeoutException {
        TempKeystore newKeys = TempKeystore.createInstance();
        Pair<PaillierPublicKey, String> pubkeys = newKeys.getPublicKeys();

        printResponse(
                contract.submitTransaction(
                        "StartQuery",
                        IdFactory.getInstance().createId(),
                        TempKeystore.paPubKeyToString(pubkeys.getP1()),
                        TempKeystore.pqPubKeyToString(pubkeys.getP2()),
                        scanNextLine("Transaction Start selected\nNumber of Operators: "),
                        scanNextLine("End Time: ")
                )
        );
    }

    public static void add(Contract contract) throws ContractException, InterruptedException, TimeoutException {
        //todo check if can be made easier to fill in
        printResponse(
                contract.submitTransaction(
                        "AddResult",
                        scanNextLine("Transaction Add selected\nID: "),
                        scanNextLine("Result: "),
                        scanNextLine("Exponent: "),
                        scanNextLine("Nonce: "),
                        scanNextLine("Number of Participants: "))
        );
    }

    public static void close(Contract contract) throws ContractException, InterruptedException, TimeoutException {
        printResponse(
                contract.submitTransaction(
                        "Close",
                        scanNextLine("Transaction Close selected\nID: ")
                )
        );
    }

    public static void retrieve(Contract contract) throws ContractException, InterruptedException, TimeoutException {
        printResponse(
                contract.submitTransaction(
                        "RetrieveDataQuery",
                        scanNextLine("Transaction Retrieve selected\nID: ")
                )
        );
    }

    public static void remove(Contract contract) throws ContractException, InterruptedException, TimeoutException {
        printResponse(
                contract.submitTransaction(
                        "RemoveDataQuery",
                        scanNextLine("Transaction Remove selected\nID: ")
                )
        );
    }

    public static void exists(Contract contract) throws ContractException, InterruptedException, TimeoutException {
        byte[] responseExists = contract.submitTransaction(
                "DataQueryExists",
                scanNextLine("Transaction Exists selected\nID: ")
        );

        System.out.printf("Asset exists: %s%n", new String(responseExists, StandardCharsets.UTF_8));
    }

    private static String scanNextLine(String message) {
        System.out.print(message);
        return scan.next();
    }

    private static void printResponse(byte[] response) {
        DataQuery serDataQuery = DataQuery.deserialize(response);
        System.out.println("Response: " + serDataQuery);
    }
}
