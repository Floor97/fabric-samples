package applications.asker;

import applications.DataQueryKeyStore;
import applications.IdFactory;
import applications.KeyStore;
import com.n1analytics.paillier.PaillierPublicKey;
import datatypes.dataquery.DataQuery;
import org.bouncycastler.pqc.crypto.ntru.NTRUEncryptionPublicKeyParameters;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractException;
import shared.Pair;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class QueryTransactions {
    private static final Scanner scan = new Scanner(System.in);

    public static Pair<String, DataQueryKeyStore> start(Contract contract) throws ContractException, InterruptedException, TimeoutException {
        String id = IdFactory.getInstance().createId();
        DataQueryKeyStore newKeys = new DataQueryKeyStore();
        Pair<PaillierPublicKey, NTRUEncryptionPublicKeyParameters> pubkeys = newKeys.getPublicKeys();

        Map<String, byte[]> trans = new HashMap<String, byte[]>();
        trans.put("paillier", KeyStore.paPubKeyToString(pubkeys.getP1()).getBytes(StandardCharsets.UTF_8));
        trans.put("post-quantum", KeyStore.pqPubKeyToBytes(pubkeys.getP2()));

        contract.createTransaction("Start").setTransient(trans).submit(
                id,
                scanNextLine("Transaction Start selected\nNumber of Operators: "),
                scanNextLine("Duration: ")
                );
        return new Pair<>(id, newKeys);
    }

    public static void close(Contract contract) throws ContractException, InterruptedException, TimeoutException {
        contract.submitTransaction(
                "Close",
                scanNextLine("Transaction Close selected\nID: ")
        );
    }

    public static void retrieve(Contract contract) throws ContractException, InterruptedException, TimeoutException {
        printResponse(
                contract.evaluateTransaction(
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
        byte[] responseExists = contract.evaluateTransaction(
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
