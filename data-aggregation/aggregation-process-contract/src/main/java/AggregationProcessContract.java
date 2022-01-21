import applications.asker.DataQueryIPFSFile;
import applications.operator.AggregationIPFSFile;
import datatypes.aggregationprocess.AggregationProcess;
import datatypes.values.IPFSConnection;
import io.ipfs.multihash.Multihash;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

@Default
@Contract(name = "aggregationprocess.pailliercontract")
public class AggregationProcessContract implements ContractInterface {

    /**
     * If not present, instantiates a new aggregation process on the ledger with the given id.
     * This id is the same as the corresponding data query. The hash from the data query object
     * is shared and used for the aggregation process. A new public key from the post-quantum
     * scheme is added. Either the StartSelection event occurs if more operator keys are needed
     * or the StartAggregating event occurs when enough operators participated. Throws an exception
     * if the aggregation already exists but is not in the selection phase.
     *
     * @param ctx                    the transaction context.
     * @param id                     the unique id of the aggregation process.
     * @param nrOperators            the number of operators required for the aggregation process.
     * @param nrExpectedParticipants the number of expected participants by the asker.
     * @param ipfsHash               the unique hash from the data query ipfs file.
     * @throws IOException when the connection with IPFS cannot be made.
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public int Start(Context ctx, String id, int nrOperators, int nrExpectedParticipants, String ipfsHash) throws IOException {
        ChaincodeStub stub = ctx.getStub();
        AggregationProcess aggregationProcess;
        if (Exists(ctx, id)) {
            aggregationProcess = AggregationProcess.deserialize(stub.getState(id));
            if ((!aggregationProcess.isSelecting()) || aggregationProcess.getNrOperatorsSelected() >= nrOperators)
                return -1;
        } else {
            DataQueryIPFSFile dataIpfsFile = IPFSConnection.getInstance().getDataQueryIPFSFile(Multihash.fromHex(ipfsHash));
            AggregationIPFSFile aggIpfsFile = new AggregationIPFSFile(
                    new BigInteger("0"),
                    new ArrayList<>()
            );
            aggIpfsFile.createHash();
            aggregationProcess = new AggregationProcess(id, nrExpectedParticipants, aggIpfsFile, 0);
        }

        aggregationProcess.addOperator();
        String serAggregationProcess;
        if (aggregationProcess.getNrOperatorsSelected() == nrOperators) {
            aggregationProcess.setAggregating();
            serAggregationProcess = aggregationProcess.serialize();
            stub.setEvent("StartAggregating", serAggregationProcess.getBytes(StandardCharsets.UTF_8));
        } else serAggregationProcess = aggregationProcess.serialize();

        stub.putStringState(id, serAggregationProcess);
        return aggregationProcess.getNrOperatorsSelected() - 1;
    }

    /**
     * Adds new data to the existent data in the aggregation process. If no current data exists the
     * data is instantiated with the new data. Throws an exception if the process is not in the
     * aggregating phase or when the given id does not correspond to a data aggregation process in
     * the world state.
     *
     * @param ctx the transaction context.
     * @param id  the unique id of the aggregation process.
     * @throws IOException when the connection with IPFS cannot be made.
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void Add(Context ctx, String id) throws IOException {
        ChaincodeStub stub = retrieveStub(ctx, id);
        AggregationProcess aggregationProcess = AggregationProcess.deserialize(stub.getState(id));

        if (!aggregationProcess.isAggregating()) throw new ChaincodeException("Process is not in aggregating phase");

        Map<String, byte[]> map = stub.getTransient();
        BigInteger newData = new BigInteger(new String(map.get("data")));
        BigInteger currentData = aggregationProcess.getIpfsFile().getData();
        String serNonces = new String(map.get("nonces"));
        BigInteger[] nonces = Arrays.stream(serNonces.substring(1, serNonces.length() - 1).split(",")).map(BigInteger::new).toArray(BigInteger[]::new);

        if (!currentData.toString().equals("0"))
            aggregationProcess.getIpfsFile().setData(currentData.add(newData));
        else aggregationProcess.getIpfsFile().setData(newData);

        aggregationProcess.getIpfsFile().addNonces(nonces);

        String serAggregationProcess;
        if (aggregationProcess.isExpectedParticipants()) {
            aggregationProcess.setClosed();
            serAggregationProcess = aggregationProcess.serialize();
            stub.setEvent("ParticipantsReached", serAggregationProcess.getBytes(StandardCharsets.UTF_8));
        } else serAggregationProcess = aggregationProcess.serialize();

        stub.putStringState(id, serAggregationProcess);
    }

    /**
     * Sets the state of the aggregation process corresponding to the given id to closed. Throws an
     * exception when the given id does not correspond to a data aggregation process in the world state.
     *
     * @param ctx the transaction context.
     * @param id  the unique id of the aggregation process.
     * @throws IOException when the connection with IPFS cannot be made.
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String Close(Context ctx, String id) throws IOException {
        ChaincodeStub stub = retrieveStub(ctx, id);

        AggregationProcess aggregationProcess = AggregationProcess.deserialize(stub.getState(id));

        aggregationProcess.setClosed();
        String serAggregationProcess = aggregationProcess.serialize();
        stub.putStringState(id, serAggregationProcess);
        return serAggregationProcess;
    }

    /**
     * The data aggregation process corresponding to the id is retrieved. Throws an exception
     * if the given id does not correspond to a data aggregation process in the world state.
     *
     * @param ctx the transaction context.
     * @param id  the unique id of the aggregation process.
     * @return the aggregation process.
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String Retrieve(Context ctx, String id) {
        ChaincodeStub stub = retrieveStub(ctx, id);
        return stub.getStringState(id);
    }

    /**
     * The data aggregation process corresponding to the id is removed from the world state. Throws
     * an exception if the given id does not correspond to a data aggregation process in the world
     * state.
     *
     * @param ctx the transaction context.
     * @param id  the unique id of the aggregation process.
     * @return the aggregation process.
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String Remove(Context ctx, String id) {
        ChaincodeStub stub = retrieveStub(ctx, id);
        String aggregationProcess = stub.getStringState(id);
        stub.delState(id);
        return aggregationProcess;
    }

    /**
     * Checks if a data aggregation process exists in the world state with the given id.
     *
     * @param ctx the transaction context.
     * @param id  the unique id of the aggregation process.
     * @return boolean indicating the existence of the aggregation process.
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean Exists(Context ctx, final String id) {
        String assetJSON = ctx.getStub().getStringState(id);
        return (assetJSON != null && !assetJSON.isEmpty());
    }

    /**
     * Checks if a data aggregation process exists in the world state with the given id. If it
     * does it returns the chaincode stub, otherwise it throws an exception.
     *
     * @param ctx the transaction context.
     * @param id  the unique id of the aggregation process.
     * @return the chaincode stub of the transaction context.
     */
    private ChaincodeStub retrieveStub(Context ctx, String id) {
        ChaincodeStub stub = ctx.getStub();
        if (!Exists(ctx, id))
            throw new ChaincodeException(String.format("Asset %s does not exist", id));

        return stub;
    }
}