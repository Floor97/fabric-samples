package applications.operator;

import encryption.KeyStore;
import datatypes.aggregationprocess.AggregationProcess;
import datatypes.dataquery.DataQuery;
import datatypes.values.EncryptedData;
import datatypes.values.EncryptedNonces;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractException;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class AggregationTransactions {

    private static final Scanner scan = new Scanner(System.in);

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

    public static void add(Contract contract, String id, EncryptedData data, EncryptedNonces nonces) throws ContractException, InterruptedException, TimeoutException {
        Map<String, byte[]> transientData = new HashMap<>();
        transientData.put("data", EncryptedData.serialize(data).getBytes(StandardCharsets.UTF_8));
        transientData.put("nonces", EncryptedNonces.serialize(nonces).getBytes(StandardCharsets.UTF_8));

        contract.createTransaction("AddData").setTransient(transientData).submit(id);
    }

    public static AggregationProcess close(Contract contractAgg, String id) throws ContractException, InterruptedException, TimeoutException {
        return AggregationProcess.deserialize(
                contractAgg.submitTransaction(
                        "Close",
                        id
                )
        );
    }

    public static AggregationProcess retrieve(Contract contract, String id) throws ContractException {
        return AggregationProcess.deserialize(
                contract.evaluateTransaction(
                        "RetrieveAggregationProcess",
                        id
                )
        );
    }

    public static AggregationProcess remove(Contract contract, String id) throws ContractException, InterruptedException, TimeoutException {
        return AggregationProcess.deserialize(
                contract.submitTransaction(
                        "RemoveAggregationProcess",
                        id
                )
        );
    }

    public static void exists(Contract contract) throws ContractException {
        printResponse(
                contract.evaluateTransaction(
                        "AggregationProcessExists",
                        scanNextLine("Transaction Exists has been selected\nID: ")
                )
        );
    }

    private static void printResponse(byte[] response) {
        AggregationProcess serAggregationProcess = AggregationProcess.deserialize(response);
        System.out.println("Response: " + serAggregationProcess);
    }

    private static String scanNextLine(String message) {
        System.out.print(message);
        return scan.next();
    }
}
