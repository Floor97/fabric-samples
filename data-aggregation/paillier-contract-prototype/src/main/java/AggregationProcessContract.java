import aggregationprocess.AggregationProcess;
import aggregationprocess.AggregationProcessData;
import aggregationprocess.AggregationProcessKeys;
import com.n1analytics.paillier.EncryptedNumber;
import com.n1analytics.paillier.PaillierContext;
import com.n1analytics.paillier.PaillierPublicKey;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import shared.Pair;

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

        AggregationProcess aggregationProcess = AggregationProcess.createInstance(
                id,
                AggregationProcessKeys.createInstance(paillierModulus, nrOperators).addOperatorKey(postQuantumPk),
                AggregationProcessData.createInstance(null, nrOperators)
        );

        byte[] serAggregationProcess = AggregationProcess.serialize(aggregationProcess);
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

        aggregationProcess.getKeystore().addOperatorKey(postQuantumPk);
        if (aggregationProcess.getKeystore().isOperatorKeysFull()) aggregationProcess.setAggregating();

        byte[] serAggregationProcess = AggregationProcess.serialize(aggregationProcess);
        stub.putState(id, serAggregationProcess);
        return new String(serAggregationProcess);
    }

    /**
     * Adds the new entry to the already aggregated data.
     *
     * @param ctx        the transaction context.
     * @param id         the unique id of the aggregation process.
     * @param cipherData the ciphertext of the new entry in the aggregation process.
     * @param exponent   the exponent of the ciphertext of the new entry.
     * @param nonces     the encrypted nonces used on the data.
     * @return the aggregation process.
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String AddData(Context ctx, String id, String cipherData, String exponent, String[] nonces) {
        ChaincodeStub stub = retrieveStub(ctx, id);
        AggregationProcess aggregationProcess = AggregationProcess.deserialize(stub.getState(id));

        if (!aggregationProcess.isAggregating()) throw new ChaincodeException("Process is not in aggregating phase");

        AggregationProcessData data = aggregationProcess.getData();
        if (aggregationProcess.getData().getCipherData() != null) {
            PaillierPublicKey pk = new PaillierPublicKey(new BigInteger(aggregationProcess.getKeystore().getPaillierModulus()));
            PaillierContext pctx = pk.createSignedContext();
            EncryptedNumber numberData = new EncryptedNumber(pctx, data.getCipherData().getP1(), data.getCipherData().getP2(), true);
            EncryptedNumber entry = new EncryptedNumber(pctx, new BigInteger(cipherData), Integer.parseInt(exponent), true);

            EncryptedNumber newData = numberData.add(entry);

            data.setCipherData(new Pair<>(newData.calculateCiphertext(), newData.getExponent()));
        } else
            aggregationProcess.getData().setCipherData(new Pair<>(new BigInteger(cipherData), Integer.valueOf(exponent)));

        data.addNonce(nonces);

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
    public void Close(Context ctx, String id) {
        ChaincodeStub stub = retrieveStub(ctx, id);

        AggregationProcess aggregationProcess = AggregationProcess.deserialize(stub.getState(id));
        if (aggregationProcess.isClosed())
            throw new ChaincodeException(String.format("Aggregation process %s is already closed", id));

        aggregationProcess.setClosed();

        byte[] serAggregationProcess = AggregationProcess.serialize(aggregationProcess);
        stub.putState(id, serAggregationProcess);
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
    public void RemoveAggregationProcess(Context ctx, String id) {
        ChaincodeStub stub = retrieveStub(ctx, id);
        stub.delState(id);
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