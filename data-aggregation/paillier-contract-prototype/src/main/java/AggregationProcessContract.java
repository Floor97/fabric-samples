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
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import java.lang.annotation.Annotation;
import java.math.BigInteger;
import java.util.ArrayList;

//todo suggestion change name to "channelp.aggregationprocess"
@Default
@Contract(name="aggregationprocess.pailliercontract")
public class AggregationProcessContract implements ContractInterface, Contract {

    /**
     * Starts a data aggregation process.
     * @param ctx the transaction context.
     * @param key the unique key of the aggregation process.
     * @param modulus the modulus of the public key used to encrypt the cData.
     * @param cData the ciphertext of the aggregated data.
     * @param expData the exponent of the ciphertext of the aggregated data.
     * @return the created aggregation process.
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String StartAggregation(Context ctx, String key, String modulus, String cData, String expData) {
        ChaincodeStub stub = ctx.getStub();
        if (AggregationProcessExists(ctx, key)) {
            throw new ChaincodeException(String.format("Aggregation process, %s, already exists", key));
        }

        AggregationProcess aggregationProcess = AggregationProcess.createInstance(key, new BigInteger(modulus),
                new BigInteger(cData), Integer.parseInt(expData), 1, "").setAggregating();

        String serAggregationProcess = getSerialized(aggregationProcess);
        stub.putStringState(key, serAggregationProcess);
        return serAggregationProcess;
    }

    /**
     * Adds the new entry to the already aggregated data.
     * @param ctx the transaction context.
     * @param key the unique key of the aggregation process.
     * @param cEntry the ciphertext of the new entry in the aggregation process.
     * @param expEntry the exponent of the ciphertext of the new entry.
     * @return the aggregation process.
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String AddData(Context ctx, String key, String cEntry, String expEntry) {
        ChaincodeStub stub = retrieveStub(ctx, key);
        AggregationProcess aggregationProcess = getDeserialized(stub.getStringState(key));

        PaillierPublicKey pk = new PaillierPublicKey(aggregationProcess.getModulus());
        PaillierContext pctx = pk.createSignedContext();
        EncryptedNumber data =  new EncryptedNumber(pctx, aggregationProcess.getcData(), aggregationProcess.getExpData(), true);
        EncryptedNumber entry = new EncryptedNumber(pctx, new BigInteger(cEntry), Integer.parseInt(expEntry), true);

        EncryptedNumber newData = data.add(entry);

        aggregationProcess.setcData(newData.calculateCiphertext());
        aggregationProcess.setExpData(newData.getExponent());
        aggregationProcess.setNrParticipants(aggregationProcess.getNrParticipants() + 1);

        String serAggregationProcess = getSerialized(aggregationProcess);
        stub.putStringState(key, serAggregationProcess);
        return serAggregationProcess;
    }

    /**
     * Closes the aggregation process of the key.
     * @param ctx the transaction context.
     * @param key the unique key of the aggregation process.
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void Close(Context ctx, String key) {
        ChaincodeStub stub = retrieveStub(ctx, key);

        AggregationProcess aggregationProcess = getDeserialized(stub.getStringState(key));
        if(aggregationProcess.isClosed())
            throw new ChaincodeException(String.format("Aggregation process %s is already closed", key));

        aggregationProcess.setClosed();

        String serAggregationProcess = getSerialized(aggregationProcess);
        stub.putStringState(key, serAggregationProcess);
    }

    /**
     * The aggregation process is retrieved and removed.
     * @param ctx the transaction context.
     * @param key the unique key of the aggregation process.
     * @return the aggregation process.
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String RetrieveAggregationProcess(Context ctx, String key) {
        ChaincodeStub stub = retrieveStub(ctx, key);
        return stub.getStringState(key);
    }

    /**
     * The aggregation process is retrieved and removed.
     * @param ctx the transaction context.
     * @param key the unique key of the aggregation process.
     * @return the aggregation process.
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public void RemoveAggregationProcess(Context ctx, String key) {
        ChaincodeStub stub = retrieveStub(ctx, key);
        stub.delState(key);
    }

    /**
     * Checks the existence of the aggregation process on the ledger.
     * @param ctx the transaction context.
     * @param key the unique key of the aggregation process.
     * @return boolean indicating the existence of the aggregation process.
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean AggregationProcessExists(Context ctx, final String key) {
        String assetJSON = ctx.getStub().getStringState(key);
        return (assetJSON != null && !assetJSON.isEmpty());
    }

    /**
     * Checks if the aggregation process exists, and returns the chaincode stub.
     * @param ctx the transaction context.
     * @param key the unique key of the aggregation process.
     * @return the chaincode stub of the transaction context.
     */
    private ChaincodeStub retrieveStub(Context ctx, String key) {
        ChaincodeStub stub = ctx.getStub();
        if(!AggregationProcessExists(ctx, key))
            throw new ChaincodeException(String.format("Asset %s does not exist", key));

        return stub;
    }

    /**
     * Serializes the aggregation process.
     * @param aggregationProcess the aggregation process that will be serialized.
     * @return the serialized aggregation process.
     */
    private String getSerialized(AggregationProcess aggregationProcess) {
        String serAggregationProcess = AggregationProcess.serialize(aggregationProcess);
        if(serAggregationProcess == null)
            throw new ChaincodeException("Unable to serialize the aggregation process");

        return serAggregationProcess;
    }

    /**
     * Deserializes the JSON into an aggregation process.
     * @param data the JSON value of the aggregation process.
     * @return the deserialized aggregation process.
     */
    private AggregationProcess getDeserialized(String data) {
        AggregationProcess aggregationProcess = AggregationProcess.deserialize(data);
        if(aggregationProcess == null)
            throw new ChaincodeException("Unable to deserialize aggregation process");

        return aggregationProcess;
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