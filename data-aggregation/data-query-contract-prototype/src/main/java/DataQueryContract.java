import datatypes.dataquery.DataQuery;
import datatypes.dataquery.DataQueryResult;
import datatypes.dataquery.DataQuerySettings;
import datatypes.values.EncryptedData;
import datatypes.values.EncryptedNonce;
import datatypes.values.EncryptedNonces;
import datatypes.values.IPFSConnection;
import datatypes.values.IPFSFile;
import io.ipfs.multihash.Multihash;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Default
@Contract(name = "query.eventcontract")
public class DataQueryContract implements ContractInterface {


    @Override
    public void beforeTransaction(Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        switch(stub.getFunction()) {
            case "StartQuery":
                List<byte[]> args = stub.getArgs();
                Map<String, byte[]> map = stub.getTransient();

                IPFSFile ipfsFile = new IPFSFile(null, Arrays.toString(map.get("paillier")), map.get("post-quantum"), new String[0],
                        "", new datatypes.values.EncryptedNonces[0]);
                ipfsFile.setHash(Multihash.fromHex(IPFSConnection.getInstance().addFile(ipfsFile)));
                stub.putState(Arrays.toString(args.get(0)), null);
                break;
            case "AddResult": break;
            case "Close": break;
            case "RetrieveDataQuery": break;
            case "RemoveDataQuery": break;
            case "DataQueryExists": break;
            default: break;
        }
    }

    /**
     * Starts a new data query.
     *
     * @param ctx             the transaction context.
     * @param id              the id of the new data query process.
     * @param paillierModulus the Paillier modulus of the public key of the invoker of the data query process.
     * @param postQuantumPk   the public key of the post-quantum encryption method.
     * @param nrOperators     the number of operators used in the process.
     * @param endTime         the end time for the process.
     * @return the new data query process as a String.
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String StartQuery(Context ctx, String id, String paillierModulus, String postQuantumPk,
                             int nrOperators, long endTime) {
        ChaincodeStub stub = ctx.getStub();
        if (DataQueryExists(ctx, id)) {
            throw new ChaincodeException(String.format("Data query, %s, already exists", id));
        }

        IPFSFile ipfsFile = new IPFSFile(null, paillierModulus, postQuantumPk, new String[0],
                "", new datatypes.values.EncryptedNonces[0]);
        ipfsFile.setHash(Multihash.fromHex(IPFSConnection.getInstance().addFile(ipfsFile)));

        DataQuery dataQuery = DataQuery.createInstance(
                id,
                DataQuerySettings.createInstance(
                        nrOperators,
                        endTime
                ),
                DataQueryResult.createInstance(0),
                ipfsFile
        );

        byte[] serDataQuery = DataQuery.serialize(dataQuery);
        stub.putState(id, serDataQuery);
        stub.setEvent("StartQuery", serDataQuery);
        return new String(serDataQuery);
    }

    /**
     * Add the result of the data query process.
     *
     * @param ctx            the transaction context.
     * @param id             the id of the data query process.
     * @param cipherData     the ciphertext of the data plus nonces encrypted with paillier.
     * @param exponent       the exponent of the ciphertext of the data encrypted with paillier.
     * @param cipherNonce    the ciphertext of the nonces encrypted using a post-quantum encryption scheme.
     * @param nrParticipants the number of participants in the data aggregation process.
     * @return the DataQuery object as a String.
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String AddResult(Context ctx, String id, String cipherData, String exponent, String cipherNonce,
                            int nrParticipants) {
        boolean first = false;
        ChaincodeStub stub = retrieveStub(ctx, id);
        DataQuery dataQuery = DataQuery.deserialize(stub.getState(id));

        if (!dataQuery.isWaiting())
            throw new ChaincodeException(String.format("Data query, %s, is not waiting", id));

        DataQueryResult res = dataQuery.getResult();
        if (res == null) {
            first = true;
            res = dataQuery.setResult(DataQueryResult.createInstance(
                    new EncryptedData(cipherData, exponent),
                    new EncryptedNonces(new EncryptedNonce[dataQuery.getSettings().getNrOperators()]),
                    nrParticipants)).getResult();
        } else if ((!res.getCipherData().getData().equals(cipherData))
                || (!res.getCipherData().getExponent().equals(exponent))
                || res.getNrParticipants() != nrParticipants)
            res.setIncFlag();
        res.addToCipherNonces(EncryptedNonce.deserialise(cipherNonce));

        byte[] serDataQuery;
        if (res.getCipherNonces().isFull()) {
            dataQuery.setDone();
            serDataQuery = DataQuery.serialize(dataQuery);
            stub.setEvent("DoneQuery", serDataQuery);
            stub.putState(id, serDataQuery);
            return new String(serDataQuery);
        } else serDataQuery = DataQuery.serialize(dataQuery);

        stub.setEvent("ResultQuery", serDataQuery);
        stub.putState(id, serDataQuery);
        return new String(serDataQuery);
    }

    /**
     * Closes the data query associated with the id.
     *
     * @param ctx the transaction context.
     * @param id  the unique id of the data query.
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String Close(Context ctx, String id) {
        ChaincodeStub stub = retrieveStub(ctx, id);

        DataQuery dataQuery = DataQuery.deserialize(stub.getState(id));
        if (dataQuery.isClosed())
            throw new ChaincodeException(String.format("Data query, %s, is already closed", id));

        dataQuery.setClosed();
        byte[] serDataQuery = DataQuery.serialize(dataQuery);
        stub.putState(id, serDataQuery);
        return new String(serDataQuery);
    }

    /**
     * The data query is retrieved.
     *
     * @param ctx the transaction context.
     * @param id  the unique id of the data query.
     * @return the data query.
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String RetrieveDataQuery(Context ctx, String id) {
        ChaincodeStub stub = retrieveStub(ctx, id);
        DataQuery dataQuery = DataQuery.deserialize(stub.getState(id));

        if (dataQuery.isWaiting())
            throw new ChaincodeException(String.format("The data query, %s, is waiting so cannot be retrieved", id));
        if (dataQuery.isDone())
            dataQuery.setClosed();

        byte[] serDataQuery = DataQuery.serialize(dataQuery);
        stub.putState(id, serDataQuery);
        return new String(serDataQuery);
    }

    /**
     * The data query is removed.
     *
     * @param ctx the transaction context.
     * @param id  the unique id of the data query.
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String RemoveDataQuery(Context ctx, String id) {
        ChaincodeStub stub = retrieveStub(ctx, id);
        byte[] serDataQuery = stub.getState(id);
        stub.delState(id);
        stub.setEvent("RemoveQuery", serDataQuery);
        return new String(serDataQuery);
    }

    /**
     * Checks the existence of the data query on the ledger.
     *
     * @param ctx the transaction context.
     * @param id  the unique id of the data query.
     * @return boolean indicating the existence of the data query.
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean DataQueryExists(Context ctx, final String id) {
        String assetJSON = ctx.getStub().getStringState(id);
        return (assetJSON != null && !assetJSON.isEmpty());
    }

    /**
     * Checks if the data query exists, and returns the chaincode stub.
     *
     * @param ctx the transaction context.
     * @param key the unique key of the data query.
     * @return the chaincode stub of the transaction context.
     */
    private ChaincodeStub retrieveStub(Context ctx, String key) {
        ChaincodeStub stub = ctx.getStub();
        if (!DataQueryExists(ctx, key))
            throw new ChaincodeException(String.format("Data query, %s, does not exist", key));

        return stub;
    }

}