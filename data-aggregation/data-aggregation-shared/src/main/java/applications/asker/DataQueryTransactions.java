package applications.asker;

import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

import static applications.operator.ParticipantTransaction.printResponse;
import static applications.operator.ParticipantTransaction.scanNextLine;

public class DataQueryTransactions {

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
    public static String start(Contract contract) throws ContractException, InterruptedException, TimeoutException {
        String nrOps = scanNextLine("Transaction Start selected\nNumber of Operators: ");
        String nrExpectedParticipants = scanNextLine("Number of expected participants: ");
        String timeLimit = scanNextLine("Time limit: ");
        String id = IdFactory.getInstance().createId();

        contract.createTransaction("Start").submit(
                id,
                nrOps,
                nrExpectedParticipants,
                timeLimit
        );
        return id;
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
     * @throws ContractException when an exception occurs in the data query contract. This occurs
     *                           when the data query referenced by the id does not exist.
     */
    public static void retrieve(Contract contract) throws ContractException, IOException {
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
     * @param id       the id of the data query asset.
     * @throws ContractException    when an exception occurs in the data query contract. This occurs
     *                              when the data query referenced by the id is already in the closed state or does not exist.
     * @throws InterruptedException thrown by the submit method.
     * @throws TimeoutException     thrown by the submit method.
     */
    public static void remove(Contract contract, String id) throws ContractException, InterruptedException, TimeoutException, IOException {
        printResponse(
                contract.submitTransaction(
                        "Remove",
                        id
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
}
