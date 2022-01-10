import com.n1analytics.paillier.EncryptedNumber;
import com.n1analytics.paillier.PaillierContext;
import com.n1analytics.paillier.PaillierPublicKey;
import datatypes.aggregationprocess.AggregationProcess;
import datatypes.values.EncryptedData;
import datatypes.values.EncryptedNonces;
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

@Default
@Contract(name = "aggregationprocess.pailliercontract")
public class AggregationProcessContract implements ContractInterface, Contract {

    /**
     * Starts a data aggregation process.
     *
     * @param ctx             the transaction context.
     * @param id              the unique id of the aggregation process.
     * @param paillierModulus the paillierModulus of the public id used to encrypt the cipherData.
     * @param postQuantumPk   the public key of the post-quantum encryption scheme.
     * @param nrOperators     the number of operators in the process.
     * @return the created aggregation process.
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String StartAggregation(Context ctx, String id, String paillierModulus, String postQuantumPk, int nrOperators) {
        ChaincodeStub stub = ctx.getStub();
        if (AggregationProcessExists(ctx, id)) {
            throw new ChaincodeException(String.format("Aggregation process, %s, already exists", id));
        }

        AggregationProcess aggregationProcess = new AggregationProcess(
                id,
        );
        aggregationProcess.getKeystore().addOperatorKey(postQuantumPk);

        byte[] serAggregationProcess;
        if(aggregationProcess.getKeystore().isOperatorKeysFull()) {
            aggregationProcess.setAggregating();
            serAggregationProcess = AggregationProcess.serialize(aggregationProcess);
            stub.setEvent("StartAggregation", serAggregationProcess);
        } else {
            serAggregationProcess = AggregationProcess.serialize(aggregationProcess);
            stub.setEvent("StartSelection", serAggregationProcess);
        }

        stub.putState(id, serAggregationProcess);
        return new String(serAggregationProcess);
    }

    /**
     * Adds a new operator key to the aggregation process.
     *
     * @param ctx           the transaction context.
     * @param id            the unique id of the aggregation process.
     * @param postQuantumPk the public key of the post-quantum encryption scheme.
     * @return the aggregation process.
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String AddOperator(Context ctx, String id, String postQuantumPk) {
        ChaincodeStub stub = retrieveStub(ctx, id);
        AggregationProcess aggregationProcess = AggregationProcess.deserialize(stub.getState(id));
        if (!aggregationProcess.isSelecting()) throw new ChaincodeException("Process is not in selection phase");

        int index = aggregationProcess.getKeystore().addOperatorKey(postQuantumPk);
        byte[] serAggregationProcess;
        if (aggregationProcess.getKeystore().isOperatorKeysFull()) {
            aggregationProcess.setAggregating();
            serAggregationProcess =  AggregationProcess.serialize(aggregationProcess);
            stub.setEvent("StartAggregation", serAggregationProcess);
        } else serAggregationProcess = AggregationProcess.serialize(aggregationProcess);

        stub.putState(id, serAggregationProcess);
        return String.valueOf(index);
    }

    /**
     * Adds the new entry to the already aggregated data.
     *
     * @param ctx        the transaction context.
     * @param id         the unique id of the aggregation process.
     * @param serNewData the ciphertext of the new entry in the aggregation process.
     * @param serNonces  the exponent of the ciphertext of the new entry.
     * @return the aggregation process.
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String AddData(Context ctx, String id, String serNewData, String serNonces) {
        ChaincodeStub stub = retrieveStub(ctx, id);
        AggregationProcess aggregationProcess = AggregationProcess.deserialize(stub.getState(id));
        EncryptedData newData = EncryptedData.deserialize(serNewData);
        EncryptedNonces nonces = EncryptedNonces.deserialize(serNonces);

        if (!aggregationProcess.isAggregating()) throw new ChaincodeException("Process is not in aggregating phase");

        AggregationProcessData currentData = aggregationProcess.getData();
        if (aggregationProcess.getData().getCipherData() != null) {
            PaillierPublicKey pk = new PaillierPublicKey(new BigInteger(aggregationProcess.getKeystore().getPaillierModulus()));
            PaillierContext pctx = pk.createSignedContext();
            EncryptedNumber numberData = new EncryptedNumber(
                    pctx,
                    new BigInteger(currentData.getCipherData().getData()),
                    Integer.parseInt(currentData.getCipherData().getExponent()),
                    true);
            EncryptedNumber entry = new EncryptedNumber(pctx, new BigInteger(newData.getData()), Integer.parseInt(newData.getExponent()), true);

            EncryptedNumber data = numberData.add(entry);

            currentData.setCipherData(new EncryptedData(data.calculateCiphertext().toString(), String.valueOf(data.getExponent())));
        } else
            aggregationProcess.getData().setCipherData(new EncryptedData(newData.getData(), newData.getExponent()));

        currentData.addNonces(nonces);

        byte[] serAggregationProcess = AggregationProcess.serialize(aggregationProcess);
        stub.putState(id, serAggregationProcess);
        return new String(serAggregationProcess);
    }

    /**
     * Closes the aggregation process of the id.
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
     * The aggregation process is retrieved and removed.
     *
     * @param ctx the transaction context.
     * @param id  the unique id of the aggregation process.
     * @return the aggregation process.
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String RetrieveAggregationProcess(Context ctx, String id) {
        ChaincodeStub stub = retrieveStub(ctx, id);
        return stub.getStringState(id);
    }

    /**
     * The aggregation process is retrieved and removed.
     *
     * @param ctx the transaction context.
     * @param id  the unique id of the aggregation process.
     * @return the aggregation process.
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String RemoveAggregationProcess(Context ctx, String id) {
        ChaincodeStub stub = retrieveStub(ctx, id);
        String aggregationProcess = stub.getStringState(id);
        stub.delState(id);
        return aggregationProcess;
    }

    /**
     * Checks the existence of the aggregation process on the ledger.
     *
     * @param ctx the transaction context.
     * @param id  the unique id of the aggregation process.
     * @return boolean indicating the existence of the aggregation process.
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean AggregationProcessExists(Context ctx, final String id) {
        String assetJSON = ctx.getStub().getStringState(id);
        return (assetJSON != null && !assetJSON.isEmpty());
    }

    /**
     * Checks if the aggregation process exists, and returns the chaincode stub.
     *
     * @param ctx the transaction context.
     * @param id  the unique id of the aggregation process.
     * @return the chaincode stub of the transaction context.
     */
    private ChaincodeStub retrieveStub(Context ctx, String id) {
        ChaincodeStub stub = ctx.getStub();
        if (!AggregationProcessExists(ctx, id))
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