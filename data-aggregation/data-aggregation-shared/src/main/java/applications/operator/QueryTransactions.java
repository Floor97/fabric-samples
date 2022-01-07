package applications.operator;

import applications.operator.generators.DataGenerator;
import datatypes.aggregationprocess.AggregationProcess;
import datatypes.aggregationprocess.AggregationProcessData;
import datatypes.dataquery.DataQuery;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class QueryTransactions {
    private static Scanner scan = new Scanner(System.in);

    public static void add(Contract contract, String id, AggregationProcessData data, byte[] condensedNonces) throws ContractException, InterruptedException, TimeoutException {

        printResponse(
                contract.submitTransaction(
                        "AddResult",
                        id,
                        data.getCipherData().getP1().toString(),
                        data.getCipherData().getP2().toString(),
                        Base64.getEncoder().encodeToString(condensedNonces),
                        String.valueOf(data.getNrParticipants())
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
