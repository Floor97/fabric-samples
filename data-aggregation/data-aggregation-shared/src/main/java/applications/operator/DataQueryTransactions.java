package applications.operator;

import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractException;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class DataQueryTransactions extends ParticipantTransaction {

    /**
     * The Add transaction in the data query contract is submitted. The obfuscated
     * data and nonces are sent as transient data. The id, number of participants
     * and index is used as regular input.
     *
     * @param contract        the data query contract.
     * @param id              the id of the data aggregation asset.
     * @param file            the IPFS file used in the aggregation process.
     * @param condensedNonces nonces encrypted with NTRUEncrypt.
     * @param index           the index the operator has in the KeyStore.
     * @throws InterruptedException thrown by the submit method.
     */
    public static void add(Contract contract, String id, AggregationIPFSFile file, BigInteger condensedNonces, int index)
            throws InterruptedException {

        Map<String, byte[]> trans = new HashMap<>();
        trans.put("data", file.getData().toString().getBytes(StandardCharsets.UTF_8));
        trans.put("nonces", condensedNonces.toString().getBytes(StandardCharsets.UTF_8));
        repeat(contract.createTransaction("Add").setTransient(trans), new String[]{
                id,
                String.valueOf(file.getNonces().size()),
                String.valueOf(index)
        });
    }

    /**
     * The Exists transaction in the data query contract is submitted.
     *
     * @param contract the data query contract.
     * @throws ContractException thrown by the evaluate method.
     */
    public static void exists(Contract contract) throws ContractException {
        byte[] responseExists = contract.evaluateTransaction(
                "DataQueryExists",
                scanNextLine("Transaction Exists selected\nID: ")
        );

        System.out.printf("Asset exists: %s%n", new String(responseExists, StandardCharsets.UTF_8));
    }
}
