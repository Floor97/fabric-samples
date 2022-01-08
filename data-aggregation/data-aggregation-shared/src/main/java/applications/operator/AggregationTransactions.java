package applications.operator;

import applications.KeyStore;
import datatypes.aggregationprocess.AggregationProcess;
import datatypes.dataquery.DataQuery;
import datatypes.values.EncryptedData;
import datatypes.values.EncryptedNonces;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractException;
import org.hyperledger.fabric.shim.ChaincodeException;

import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class AggregationTransactions {

    private static Scanner scan = new Scanner(System.in);

    public static OperatorKeyStore start(Contract contractAgg, Contract contractQuery, DataQuery dataQuery) throws ContractException, InterruptedException, TimeoutException {
        OperatorKeyStore keystore = OperatorKeyStore.createInstance();
        keystore.setIndex(0);
        printResponse(
                contractAgg.submitTransaction(
                        "StartAggregation",
                        dataQuery.getId(),
                        dataQuery.getSettings().getPaillierModulus(),
                        KeyStore.pqPubKeyToString(keystore.getPublicKey()),
                        String.valueOf(dataQuery.getSettings().getNrOperators())
                )
        );

        return keystore;
    }

    public static OperatorKeyStore addop(Contract contract, String id) throws ContractException, InterruptedException, TimeoutException {
        OperatorKeyStore keystore = OperatorKeyStore.createInstance();
        //todo catch chaincode exception
            byte[] index =
                    contract.submitTransaction(
                            "AddOperator",
                            id,
                            KeyStore.pqPubKeyToString(keystore.getPublicKey())
                    );
        keystore.setIndex(Integer.parseInt(new String(index)));
        return keystore;
    }

    public static void adddata(Contract contract, String id, String cipherData, String exponent, EncryptedNonces nonces) throws ContractException, InterruptedException, TimeoutException {
        printResponse(
                contract.submitTransaction(
                        "AddData",
                        id,
                        EncryptedData.serialize(new EncryptedData(cipherData, exponent)),
                        EncryptedNonces.serialize(nonces)
                )
        );
    }

    public static AggregationProcess close(Contract contractAgg, String id) throws ContractException, InterruptedException, TimeoutException {
        return AggregationProcess.deserialize(
                contractAgg.submitTransaction(
                        "Close",
                        id
                )
        );
    }

    public static AggregationProcess retrieve(Contract contract, String id) throws ContractException, InterruptedException, TimeoutException {
        return AggregationProcess.deserialize(
                contract.submitTransaction(
                        "RetrieveAggregationProcess",
                        id
                )
        );
    }

    public static void remove(Contract contract, String id) throws ContractException, InterruptedException, TimeoutException {
        printResponse(
                contract.submitTransaction(
                        "RemoveAggregationProcess",
                        id
                )
        );
    }

    public static void exists(Contract contract) throws ContractException, InterruptedException, TimeoutException {
        printResponse(
                contract.submitTransaction(
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
