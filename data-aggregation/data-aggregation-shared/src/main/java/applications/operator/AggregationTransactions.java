package applications.operator;

import datatypes.aggregationprocess.AggregationProcess;
import datatypes.dataquery.DataQuery;
import datatypes.values.EncryptedData;
import datatypes.values.EncryptedNonces;
import encryption.KeyStore;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractException;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class AggregationTransactions {

    private static final Scanner scan = new Scanner(System.in);

    /**
     * The Start transaction in the aggregation process contract is submitted. The NTRUEncrypt
     * public key of the operator is sent as transient data. The id, number of operators and
     * hash of the IPFS file used in the data query process are used as regular input.
     *
     * @param contractAgg the aggregation process contract.
     * @param dataQuery   the data query contract.
     * @return the new OperatorKeyStore the operator generated for the process.
     * @throws ContractException    when an exception occurs in the aggregation process contract.
     *                              An exception occurs when the aggregation process asset already exists but is not in
     *                              selection phase.
     * @throws InterruptedException thrown by the submit method.
     * @throws TimeoutException     thrown by the submit method.
     */
    public static OperatorKeyStore start(Contract contractAgg, DataQuery dataQuery) throws ContractException, InterruptedException, TimeoutException {
        OperatorKeyStore keystore = OperatorKeyStore.createInstance();
        Map<String, byte[]> transientData = new HashMap<>();

        transientData.put("operator", KeyStore.pqPubKeyToString(keystore.getPublicKey()).getBytes(StandardCharsets.UTF_8));
        byte[] index = contractAgg.createTransaction("Start").setTransient(transientData).submit(
                dataQuery.getId(),
                String.valueOf(dataQuery.getSettings().getNrOperators()),
                dataQuery.getIpfsFile().getHash().toHex()
        );
        keystore.setIndex(Integer.parseInt(new String(index)));

        return keystore;
    }

    /**
     * The Add transaction in the aggregation process contract is submitted. The obfuscated
     * data and nonces are sent as transient data. The id is regular input.
     *
     * @param contract the aggregation process contract.
     * @param id       the id of the data aggregation asset.
     * @param data     the obfuscated data that will be added to the data in the data aggregation
     *                 asset.
     * @param nonces   the nonces that will be added to the list of nonces in the data aggregation
     *                 asset.
     * @throws ContractException    when an exception occurs in the aggregation process contract.
     *                              An exception occurs when the aggregation process asset is not in the aggregation phase,
     *                              or does not exist.
     * @throws InterruptedException thrown by the submit method.
     * @throws TimeoutException     thrown by the submit method.
     */
    public static void add(Contract contract, String id, EncryptedData data, EncryptedNonces nonces) throws ContractException, InterruptedException, TimeoutException {
        Map<String, byte[]> transientData = new HashMap<>();
        transientData.put("data", EncryptedData.serialize(data).getBytes(StandardCharsets.UTF_8));
        transientData.put("nonces", EncryptedNonces.serialize(nonces).getBytes(StandardCharsets.UTF_8));

        contract.createTransaction("AddData").setTransient(transientData).submit(id);
    }

    /**
     * The Close transaction in the aggregation process contract is submitted.
     *
     * @param contractAgg the aggregation process contract.
     * @param id          the id of the aggregation process asset.
     * @return the response to the Close transaction, the AggregationProcess.
     * @throws ContractException    when an exception occurs in the aggregation process contract.
     *                              An exception occurs when the aggregation process asset is already in the closed state,
     *                              or does not exist.
     * @throws InterruptedException thrown by the submit method.
     * @throws TimeoutException     thrown by the submit method.
     */
    public static AggregationProcess close(Contract contractAgg, String id) throws ContractException, InterruptedException, TimeoutException {
        return AggregationProcess.deserialize(
                contractAgg.submitTransaction(
                        "Close",
                        id
                )
        );
    }

    /**
     * The Retrieve transaction in the aggregation process contract is evaluated.
     *
     * @param contract the aggregation process contract.
     * @param id       the id of the aggregation process asset.
     * @return the response to the Retrieve transaction, the AggregationProcess.
     * @throws ContractException when an exception occurs in the aggregation process contract.
     *                           An exception occurs when the aggregation process asset does not exist.
     */
    public static AggregationProcess retrieve(Contract contract, String id) throws ContractException {
        return AggregationProcess.deserialize(
                contract.evaluateTransaction(
                        "RetrieveAggregationProcess",
                        id
                )
        );
    }

    /**
     * The Remove transaction in the aggregation process contract is evaluated.
     *
     * @param contract the aggregation process contract.
     * @param id       the id of the aggregation process asset.
     * @return the response to the Retrieve transaction, the AggregationProcess.
     * @throws ContractException    when an exception occurs in the aggregation process contract.
     *                              An exception occurs when the aggregation process asset does not exist.
     * @throws InterruptedException thrown by the submit method.
     * @throws TimeoutException     thrown by the submit method.
     */
    public static AggregationProcess remove(Contract contract, String id) throws ContractException, InterruptedException, TimeoutException {
        return AggregationProcess.deserialize(
                contract.submitTransaction(
                        "RemoveAggregationProcess",
                        id
                )
        );
    }

    /**
     * The Exists transaction in the aggregation process contract is evaluated.
     *
     * @param contract the aggregation process contract.
     * @throws ContractException thrown by the submit method.
     */
    public static void exists(Contract contract) throws ContractException {
        printResponse(
                contract.evaluateTransaction(
                        "AggregationProcessExists",
                        scanNextLine("Transaction Exists has been selected\nID: ")
                )
        );
    }

    /**
     * Helper method that deserializes the response of a transaction and prints it.
     *
     * @param response the response of a aggregation process contract transaction.
     */
    private static void printResponse(byte[] response) {
        AggregationProcess serAggregationProcess = AggregationProcess.deserialize(response);
        System.out.println("Response: " + serAggregationProcess);
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
}
