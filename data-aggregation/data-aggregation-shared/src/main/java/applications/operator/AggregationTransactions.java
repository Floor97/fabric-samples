package applications.operator;

import datatypes.aggregationprocess.AggregationProcess;
import datatypes.dataquery.DataQuery;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractException;

import java.util.*;
import java.util.concurrent.TimeoutException;

public class AggregationTransactions {

    private static Scanner scan = new Scanner(System.in);

    public static void start(Contract contractAgg, Contract contractQuery, DataQuery dataQuery) throws ContractException, InterruptedException, TimeoutException {
        OperatorKeyStore keystore = OperatorKeyStore.createInstance();
        printResponse(
                contractAgg.submitTransaction(
                        "StartAggregation",
                        dataQuery.getId(),
                        dataQuery.getSettings().getPaillierModulus(),
                        keystore.getPublicKey(),
                        String.valueOf(dataQuery.getSettings().getNrOperators())
                )
        );

        Timer timer = new Timer();
        TimerTask action = new TimerTask() {
            public void run() {
                try {
                    AggregationTransactions.close(contractAgg, contractQuery, dataQuery.getId());
                } catch (ContractException | InterruptedException | TimeoutException e) {
                    e.printStackTrace();
                }
            }

        };
        timer.schedule(action, dataQuery.getSettings().getEndTime());
        //todo take into account time limit
    }

    public static void addop(Contract contract, String id) throws ContractException, InterruptedException, TimeoutException {
        OperatorKeyStore keystore = OperatorKeyStore.createInstance();
        //todo catch chaincode exception
        printResponse(
                contract.submitTransaction(
                        "AddOperator",
                        id,
                        keystore.getPostQuantumKeys()
                )
        );
    }

    public static void adddata(Contract contract, String id, String cipherData, int exponent, byte[][] nonces) throws ContractException, InterruptedException, TimeoutException {
        printResponse(
                contract.submitTransaction(
                        "AddData",
                        id,
                        cipherData,
                        String.valueOf(exponent),
                        Base64.getEncoder().encodeToString(nonces)
                )
        );
    }

    public static void close(Contract contractAgg, Contract contractQuery, String id) throws ContractException, InterruptedException, TimeoutException {
        AggregationProcess aggregationProcess = AggregationProcess.deserialize(
                contractAgg.submitTransaction(
                        "Close",
                        id
                )
        );
        QueryTransactions.add(contractQuery, id, aggregationProcess.getData());
    }

    public static void retrieve(Contract contract, String id) throws ContractException, InterruptedException, TimeoutException {
        printResponse(
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

    public static void exists(Contract contract, String id) throws ContractException, InterruptedException, TimeoutException {
        printResponse(
                contract.submitTransaction(
                        "AggregationProcessExists",
                        id
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
