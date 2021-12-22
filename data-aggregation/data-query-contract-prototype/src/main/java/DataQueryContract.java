import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;

//todo suggestion change name to "channela.dataquery"
@Default
@Contract(name="eventcontract")
public class DataQueryContract implements ContractInterface {

    /**
     * Starts a data query.
     * @param ctx the transaction context.
     * @param queryID the unique queryID of the data query.
     * @param modulus the modulus of the public key used to encrypt the result.
     * @param result the ciphertext of the result.
     * @param expResult the exponent of the ciphertext of the result.
     * @param timeLimit the time limit for the data query.
     * @param nrParticipants the number of participants required in the data query.
     * @return the created data query.
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String StartQuery(Context ctx, String queryID, String modulus, String result, String expResult,
                                   String timeLimit, String nrParticipants) {
        ChaincodeStub stub = ctx.getStub();
        if (DataQueryExists(ctx, queryID)) {
            throw new ChaincodeException(String.format("Data query, %s, already exists", queryID));
        }

        DataQuery dataQuery = DataQuery.createInstance(queryID, modulus, result, expResult, timeLimit, nrParticipants,
                "").setWaiting();

        String serDataQuery = getSerialized(dataQuery);
        stub.putStringState(queryID, serDataQuery);
        return serDataQuery;
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String AddResult(Context ctx, String queryID, String result, String expResult, String nrParticipants) {
        ChaincodeStub stub = retrieveStub(ctx, queryID);

        DataQuery dataQuery = getDeserialized(stub.getStringState(queryID));
        if(!dataQuery.isWaiting())
            throw new ChaincodeException(String.format("Data query, %s, has status %s", queryID, dataQuery.getStatus()));

        dataQuery.setResult(result);
        dataQuery.setExpResult(expResult);
        dataQuery.setNrParticipants(nrParticipants);
        dataQuery.setDone();

        String serDataQuery = getSerialized(dataQuery);
        stub.putStringState(queryID, serDataQuery);
        return serDataQuery;
    }

    /**
     * Closes the data query associated with the key.
     * @param ctx the transaction context.
     * @param key the unique key of the data query.
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void Close(Context ctx, String key) {
        ChaincodeStub stub = retrieveStub(ctx, key);

        DataQuery dataQuery = getDeserialized(stub.getStringState(key));
        if(dataQuery.isClosed())
            throw new ChaincodeException(String.format("Data query, %s, is already closed", key));

        dataQuery.setClosed();

        stub.putStringState(key, getSerialized(dataQuery));
    }

    /**
     * The data query is retrieved.
     * @param ctx the transaction context.
     * @param queryID the unique queryID of the data query.
     * @return the data query.
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String RetrieveDataQuery(Context ctx, String queryID) {
        ChaincodeStub stub = retrieveStub(ctx, queryID);
        DataQuery dataQuery = getDeserialized(stub.getStringState(queryID));

        if(dataQuery.isWaiting())
            throw new ChaincodeException(String.format("The data query, %s, is waiting so cannot be retrieved", queryID));
        if(dataQuery.isDone())
            dataQuery.setClosed();

        String serDataQuery = getSerialized(dataQuery);
        stub.putStringState(queryID, serDataQuery);
        return serDataQuery;
    }

    /**
     * The data query is removed.
     * @param ctx the transaction context.
     * @param queryID the unique queryID of the data query.
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public void RemoveDataQuery(Context ctx, String queryID) {
        ChaincodeStub stub = retrieveStub(ctx, queryID);
        stub.delState(queryID);
    }

    /**
     * Checks the existence of the data query on the ledger.
     * @param ctx the transaction context.
     * @param key the unique key of the data query.
     * @return boolean indicating the existence of the data query.
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean DataQueryExists(Context ctx, final String key) {
        String assetJSON = ctx.getStub().getStringState(key);
        return (assetJSON != null && !assetJSON.isEmpty());
    }

    /**
     * Checks if the data query exists, and returns the chaincode stub.
     * @param ctx the transaction context.
     * @param key the unique key of the data query.
     * @return the chaincode stub of the transaction context.
     */
    private ChaincodeStub retrieveStub(Context ctx, String key) {
        ChaincodeStub stub = ctx.getStub();
        if(!DataQueryExists(ctx, key))
            throw new ChaincodeException(String.format("Data query, %s, does not exist", key));

        return stub;
    }

    /**
     * Serializes the data query.
     * @param dataQuery the data query that will be serialized.
     * @return the serialized data query.
     */
    private String getSerialized(DataQuery dataQuery) {
        String serDataQuery = DataQuery.serialize(dataQuery);
        if(serDataQuery == null)
            throw new ChaincodeException("Unable to serialize the data query");

        return serDataQuery;
    }

    /**
     * Deserializes the JSON into a data query.
     * @param data the JSON value of the data query.
     * @return the deserialized data query.
     */
    private DataQuery getDeserialized(String data) {
        DataQuery dataQuery = DataQuery.deserialize(data);
        if(dataQuery == null)
            throw new ChaincodeException("Unable to deserialize data query");

        return dataQuery;
    }
}