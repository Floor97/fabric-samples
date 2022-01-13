package applications.operator;

import datatypes.values.EncryptedData;
import datatypes.values.EncryptedNonce;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractException;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class QueryTransactions {
    private static final Scanner scan = new Scanner(System.in);

    public static void add(Contract contract, String id, EncryptedData data, EncryptedNonce condensedNonces, int nrParticipants, int index) throws ContractException, InterruptedException, TimeoutException {
        Map<String, byte[]> trans = new HashMap<>();
        trans.put("data", EncryptedData.serialize(data).getBytes(StandardCharsets.UTF_8));
        trans.put("nonces", EncryptedNonce.serialize(condensedNonces).getBytes(StandardCharsets.UTF_8));
        contract.createTransaction("Add").setTransient(trans).submit(
                id,
                String.valueOf(nrParticipants),
                String.valueOf(index)
        );
    }

    public static void exists(Contract contract) throws ContractException {
        byte[] responseExists = contract.evaluateTransaction(
                "DataQueryExists",
                scanNextLine("Transaction Exists selected\nID: ")
        );

        System.out.printf("Asset exists: %s%n", new String(responseExists, StandardCharsets.UTF_8));
    }

    private static String scanNextLine(String message) {//todo create interface of Transactions with scanNextLine and printResponse methods
        System.out.print(message);
        return scan.next();
    }
}
