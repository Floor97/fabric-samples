package applications.asker;

import com.n1analytics.paillier.PaillierPublicKey;
import datatypes.dataquery.DataQuery;
import datatypes.values.Pair;
import encryption.KeyStore;
import org.bouncycastler.pqc.crypto.ntru.NTRUEncryptionPublicKeyParameters;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractException;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class DataQueryTransactions {
    private static final Scanner scan = new Scanner(System.in);

    /**
     * The Start transaction in the data query contract is submitted. The Paillier and NTRUEncrypt
     * public keys are sent as transient data. The id, number of operator and time limit are sent
     * as normal input.
     *
     * @param contract the data query contract.
     * @return a Pair object containing the id and the DataQueryKeyStore.
     * @throws ContractException    when an exception occurs in the data query contract. An exception
     *                              occurs when the id is already in use.
     * @throws InterruptedException thrown by the submit method.
     * @throws TimeoutException     thrown by the submit method.
     */
    public static Pair<String, DataQueryKeyStore> start(Contract contract) throws ContractException, InterruptedException, TimeoutException {
        String id = IdFactory.getInstance().createId();
        DataQueryKeyStore newKeys = new DataQueryKeyStore();
        Pair<PaillierPublicKey, NTRUEncryptionPublicKeyParameters> pubkeys = newKeys.getPublicKeys();

        Map<String, byte[]> trans = new HashMap<>();
        trans.put("paillier", KeyStore.paPubKeyToString(pubkeys.getP1()).getBytes(StandardCharsets.UTF_8));
        trans.put("post-quantum", KeyStore.pqPubKeyToBytes(pubkeys.getP2()));

        contract.createTransaction("Start").setTransient(trans).submit(
                id,
                scanNextLine("Transaction Start selected\nNumber of Operators: "),
                scanNextLine("Duration: ")
        );
        return new Pair<>(id, newKeys);
    }

    /**
     * The Close transaction in the data query contract is submitted.
     *
     * @param contract the data query contract.
     * @throws ContractException    when an exception occurs in the data query contract. This occurs
     *                              when the data query referenced by the id is already in the closed state.
     * @throws InterruptedException thrown by the submit method.
     * @throws TimeoutException     thrown by the submit method.
     */
    public static void close(Contract contract) throws ContractException, InterruptedException, TimeoutException {
        contract.submitTransaction(
                "Close",
                scanNextLine("Transaction Close selected\nID: ")
        );
    }

    /**
     * The Retrieve transaction in the data query contract is evaluated.
     *
     * @param contract the data query contract.
     * @throws ContractException    when an exception occurs in the data query contract. This occurs
     *                              when the data query referenced by the id does not exist.
     * @throws InterruptedException thrown by the submit method.
     * @throws TimeoutException     thrown by the submit method.
     */
    public static void retrieve(Contract contract) throws ContractException, InterruptedException, TimeoutException {
        printResponse(
                contract.evaluateTransaction(
                        "Retrieve",
                        scanNextLine("Transaction Retrieve selected\nID: ")
                )
        );
    }

    /**
     * The Remove transaction in the data query contract is submitted.
     *
     * @param contract the data query contract.
     * @throws ContractException    when an exception occurs in the data query contract. This occurs
     *                              when the data query referenced by the id is already in the closed state or does not exist.
     * @throws InterruptedException thrown by the submit method.
     * @throws TimeoutException     thrown by the submit method.
     */
    public static void remove(Contract contract) throws ContractException, InterruptedException, TimeoutException {
        printResponse(
                contract.submitTransaction(
                        "Remove",
                        scanNextLine("Transaction Remove selected\nID: ")
                )
        );
    }

    /**
     * The Exists transaction in the data query contract is submitted.
     *
     * @param contract the data query contract.
     * @throws ContractException when an exception occurs in the data query contract.
     */
    public static void exists(Contract contract) throws ContractException {
        byte[] responseExists = contract.evaluateTransaction(
                "Exists",
                scanNextLine("Transaction Exists selected\nID: ")
        );

        System.out.printf("Asset exists: %s%n", new String(responseExists, StandardCharsets.UTF_8));
    }

    /**
     * Helper method that prints the message and sends back the next input on System.in.
     *
     * @param message the message that will be printed.
     * @return the next input on System.in.
     */
    private static String scanNextLine(String message) {
        System.out.print(message);
        return scan.next();
    }

    /**
     * Helper method that deserializes the response of a transaction and prints it.
     *
     * @param response the response of a data query contract transaction.
     */
    private static void printResponse(byte[] response) {
        DataQuery serDataQuery = DataQuery.deserialize(response);
        System.out.println("Response: " + serDataQuery);
    }
}
