import applications.KeyStore;
import com.n1analytics.paillier.EncryptedNumber;
import com.n1analytics.paillier.PaillierContext;
import com.n1analytics.paillier.PaillierPublicKey;
import datatypes.aggregationprocess.AggregationProcess;
import datatypes.dataquery.DataQuery;
import datatypes.values.EncryptedData;
import datatypes.values.EncryptedNonces;
import datatypes.values.IPFSConnection;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.lang.annotation.Annotation;
import java.math.BigInteger;
import java.util.Map;

@Default
@Contract(name = "aggregationprocess.pailliercontract")
public class AggregationProcessContract implements ContractInterface, Contract {

    /**
     * Instantiates a new aggregation process on the ledger with the given id. This id is the same
     * as the corresponding data query. The hash from the data query object is shared and used for
     * the aggregation process. A new public key from the post-quantum scheme is added. Either
     * the StartSelection event occurs if more operator keys are needed than one, or the
     * StartAggregating event occurs when enough operators participated.
     *
     * @param ctx the transaction context.
     * @param id  the unique id of the aggregation process.
     * @param ipfsHash the unique hash from the data query ipfs file.
     * @return the created aggregation process.
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public byte[] Start(Context ctx, String id, String ipfsHash) {
        ChaincodeStub stub = ctx.getStub();
        Map<String, byte[]> map = stub.getTransient();

        if (Exists(ctx, id))
            throw new ChaincodeException(String.format("Aggregation process, %s, already exists", id));

        AggregationProcess aggregationProcess = new AggregationProcess(id, IPFSConnection.getInstance().getFile(ipfsHash));
        aggregationProcess.getIpfsFile().addOperatorKey(KeyStore.pqToPubKey(map.get("post-quantum")));

        byte[] serAggregationProcess;
        String event = "StartSelection";
        if (aggregationProcess.getIpfsFile().isFull()) {
            event = "StartAggregating";
            aggregationProcess.setAggregating();
        }

        serAggregationProcess = AggregationProcess.serialize(aggregationProcess);
        stub.setEvent(event, serAggregationProcess);
        stub.putState(id, serAggregationProcess);
        return serAggregationProcess;
    }

    /**
     * A new public key for the post-quantum encryption scheme is added to the operator keys. The
     * StartAggregation event occurs when all operator keys are gathered. Throws an exception when
     * the data aggregation process is not in the selecting phase or when the given id does not
     * correspond to a data aggregation process in the world state.
     *
     * @param ctx           the transaction context.
     * @param id            the unique id of the aggregation process.
     * @return the aggregation process.
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public int AddOperator(Context ctx, String id) {
        ChaincodeStub stub = retrieveStub(ctx, id);
        Map<String, byte[]> map = stub.getTransient();
        AggregationProcess aggregationProcess = AggregationProcess.deserialize(stub.getState(id));

        if (!aggregationProcess.isSelecting()) throw new ChaincodeException("Process is not in selection phase");

        int index = aggregationProcess.getIpfsFile().addOperatorKey(KeyStore.pqToPubKey(map.get("post-quantum")));
        byte[] serAggregationProcess;
        if (aggregationProcess.getIpfsFile().isFull()) {
            aggregationProcess.setAggregating();
            serAggregationProcess = AggregationProcess.serialize(aggregationProcess);
            stub.setEvent("StartAggregation", serAggregationProcess);
        } else serAggregationProcess = AggregationProcess.serialize(aggregationProcess);

        stub.putState(id, serAggregationProcess);
        return index;
    }

    /**
     * Adds new data to the existent data in the aggregation process. If no current data exists the
     * data is instantiated with the new data. Throws an exception if the process is not in the
     * aggregating phase or when the given id does not correspond to a data aggregation process in
     * the world state.
     *
     * @param ctx        the transaction context.
     * @param id         the unique id of the aggregation process.
     * @return the aggregation process.
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String AddData(Context ctx, String id) {
        ChaincodeStub stub = retrieveStub(ctx, id);
        AggregationProcess aggregationProcess = AggregationProcess.deserialize(stub.getState(id));

        if (!aggregationProcess.isAggregating()) throw new ChaincodeException("Process is not in aggregating phase");

        Map<String, byte[]> map = stub.getTransient();
        EncryptedData newData = EncryptedData.deserialize(map.get("data"));
        EncryptedData currentData = aggregationProcess.getIpfsFile().getData();
        EncryptedNonces nonces = EncryptedNonces.deserialize(map.get("nonces"));

        if (currentData.getData() != null) {
            PaillierPublicKey pk = new PaillierPublicKey(new BigInteger(aggregationProcess.getIpfsFile().getPaillierKey()));
            PaillierContext pctx = pk.createSignedContext();
            EncryptedNumber encCurrentData = new EncryptedNumber(pctx, new BigInteger(currentData.getData()),
                    Integer.parseInt(currentData.getExponent()), true);
            EncryptedNumber encNewData = new EncryptedNumber(pctx, new BigInteger(newData.getData()),
                    Integer.parseInt(newData.getExponent()), true);

            encCurrentData = encCurrentData.add(encNewData);
            currentData.setData(encCurrentData.calculateCiphertext().toString()).setExponent(String.valueOf(encCurrentData.getExponent()));
        } else currentData.setData(newData.getData()).setExponent(newData.getExponent());

        aggregationProcess.getIpfsFile().addNonces(nonces);

        byte[] serAggregationProcess = AggregationProcess.serialize(aggregationProcess);
        stub.putState(id, serAggregationProcess);
        return new String(serAggregationProcess);
    }

    /**
     * Sets the state of the aggregation process corresponding to the given id to closed. Throws an
     * exception if the process was already in the closed state, or when the given id does not
     * correspond to a data aggregation process in the world state.
     *
     * @param ctx the transaction context.
     * @param id  the unique id of the aggregation process.
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String Close(Context ctx, String id) {
        ChaincodeStub stub = retrieveStub(ctx, id);

        AggregationProcess aggregationProcess = AggregationProcess.deserialize(stub.getState(id));
        if (aggregationProcess.isClosed())
            throw new ChaincodeException(String.format("Aggregation process %s is already closed", id));

        aggregationProcess.setClosed();
        byte[] serAggregationProcess = AggregationProcess.serialize(aggregationProcess);
        stub.putState(id, serAggregationProcess);
        return new String(serAggregationProcess);
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

    //todo implement Contract methods.
    @Override
    public Info info() {
        return null;
    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public String transactionSerializer() {
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String toString() {
        return null;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return null;
    }
}