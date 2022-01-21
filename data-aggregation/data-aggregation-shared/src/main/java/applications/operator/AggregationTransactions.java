package applications.operator;

import datatypes.aggregationprocess.AggregationProcess;
import datatypes.dataquery.DataQuery;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractException;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class AggregationTransactions extends ParticipantTransaction {

    /**
     * The Start transaction in the aggregation process contract is submitted. The NTRUEncrypt
     * public key of the operator is sent as transient data. The id, number of operators and
     * hash of the IPFS file used in the data query process are used as regular input.
     *
     * @param contractAgg the aggregation process contract.
     * @param nrExpectedParticipants the number of expected participants by the asker.
     * @param dataQuery   the data query contract.
     * @return the index of the operator.
     * @throws InterruptedException thrown by the submit method.
     */
    public static int start(Contract contractAgg, int nrExpectedParticipants, DataQuery dataQuery) throws InterruptedException, IOException {
        byte[] index = repeat(contractAgg.createTransaction("Start"), new String[]{
                dataQuery.getId(),
                String.valueOf(dataQuery.getSettings().getNrOperators()),
                String.valueOf(nrExpectedParticipants),
                dataQuery.getIpfsFile().getHash().toHex()
        }, dataQuery.getId());
        return Integer.parseInt(new String(index));
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
    public static void add(Contract contract, String id, BigInteger data, BigInteger[] nonces) throws ContractException, InterruptedException, TimeoutException {
        Map<String, byte[]> transientData = new HashMap<>();
        StringBuilder builder = new StringBuilder();
        builder.append("[")
                .append(Arrays.stream(nonces).map(BigInteger::toString).collect(Collectors.joining(",")))
                .append("]");
        transientData.put("data", data.toString().getBytes(StandardCharsets.UTF_8));
        transientData.put("nonces", builder.toString().getBytes(StandardCharsets.UTF_8));

        repeat(contract.createTransaction("Add").setTransient(transientData), new String[]{id}, id);
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
    public static AggregationProcess close(Contract contractAgg, String id) throws ContractException, InterruptedException, TimeoutException, IOException {
        byte[] response = contractAgg.submitTransaction(
                "Close",
                id
        );
        return AggregationProcess.deserialize(response);
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
    public static AggregationProcess retrieve(Contract contract, String id) throws ContractException, IOException {
        return AggregationProcess.deserialize(
                contract.evaluateTransaction(
                        "Retrieve",
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
    public static AggregationProcess remove(Contract contract, String id) throws ContractException, InterruptedException, TimeoutException, IOException {
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
    public static void exists(Contract contract) throws ContractException, IOException {
        printResponse(
                contract.evaluateTransaction(
                        "Exists",
                        scanNextLine("Transaction Exists has been selected\nID: ")
                )
        );
    }
}
