import applications.KeyStore;
import datatypes.dataquery.DataQuery;
import datatypes.dataquery.DataQuerySettings;
import datatypes.values.EncryptedData;
import datatypes.values.EncryptedNonce;
import datatypes.values.EncryptedNonces;
import datatypes.values.IPFSFile;
import org.bouncycastler.pqc.crypto.ntru.NTRUEncryptionPublicKeyParameters;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.util.Map;

@Default
@Contract(name = "query.eventcontract")
public class DataQueryContract implements ContractInterface {

    /**
     * Instantiates a new data query process on the ledger. The keys of the asker are provided and stored
     * on IPFS. The number of operators and duration of the process are also specified in the data query
     * object. The event StartQuery is set in the transaction. Throws an exception if the suggested id
     * of the data query process is already in use.
     *
     * @param ctx         the transaction context.
     * @param id          the id of the new data query process.
     * @param nrOperators the number of operators used in the process.
     * @param duration    the end time for the process.
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void Start(Context ctx, String id, int nrOperators, long duration) {
        System.out.println("Starting transaction Start");
        ChaincodeStub stub = ctx.getStub();
        Map<String, byte[]> trans = stub.getTransient();
        System.out.println("Got transient data");

        if (Exists(ctx, id))
            throw new ChaincodeException(String.format("Data query, %s, already exists", id));

        System.out.println("id does not exist yet");

        IPFSFile ipfsFile = new IPFSFile.IPFSFileBuilder(
                new String(trans.get("paillier")),
                KeyStore.pqToPubKey(trans.get("post-quantum")))
                .setOperatorKeys(new NTRUEncryptionPublicKeyParameters[nrOperators])
                .setData(new EncryptedData("null", "null"))
                .setNonces(new datatypes.values.EncryptedNonces[0])
                .build();
        System.out.println("Made ipfs file");

        DataQuery dataQuery = new DataQuery(id, new DataQuerySettings(nrOperators, duration), ipfsFile);
        System.out.println("Made dataquery file");

        byte[] serDataQuery = DataQuery.serialize(dataQuery);
        stub.setEvent("StartQuery", serDataQuery);
        stub.putState(id, serDataQuery);
    }

    /**
     * Sets the data and number of participants, and adds the respective nonce of the operator. If the data
     * and number of participants is already set, checks if it corresponds to the provided data and number
     * of participants. If this is not the case, sets the inconsistency flag of data query. If all
     * operators have added their nonces, the state of the DataQuery is set to done and a corresponding
     * event is set. Throws an exception if the state of the process is not waiting, or if the data query
     * process does not exist.
     *
     * @param ctx            the transaction context.
     * @param id             the id of the data query process.
     * @param nrParticipants the number of participants in the data aggregation process.
     * @return the DataQuery object as a String.
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public byte[] Add(Context ctx, String id, int nrParticipants, int index) {
        ChaincodeStub stub = retrieveStub(ctx, id);
        Map<String, byte[]> trans = stub.getTransient();
        DataQuery dataQuery = DataQuery.deserialize(stub.getState(id));

        if (!dataQuery.isWaiting())
            throw new ChaincodeException(String.format("Data query, %s, is not waiting", id));

        IPFSFile ipfsFile = dataQuery.getIpfsFile();
        EncryptedData encData = EncryptedData.deserialize(new String(trans.get("data")));

        if (ipfsFile.getData().getData().equals("null")) {
            dataQuery.getIpfsFile().setData(encData);
        } else if (!ipfsFile.getData().getData().equals(encData.getData())
                || !ipfsFile.getData().getExponent().equals(encData.getExponent())
                || dataQuery.getIpfsFile().getNonces().length != nrParticipants)
            dataQuery.setIncFlag();
        ipfsFile.getNonces()[index] = new EncryptedNonces(EncryptedNonce.deserialize(new String(trans.get("nonces"))));

        byte[] serDataQuery;
        if (ipfsFile.getNonces().length == nrParticipants) {
            dataQuery.setDone();
            serDataQuery = DataQuery.serialize(dataQuery);
            stub.setEvent("DoneQuery", serDataQuery);
        } else serDataQuery = DataQuery.serialize(dataQuery);

        stub.putState(id, serDataQuery);
        return serDataQuery;
    }

    /**
     * Sets state of the data query associated with the corresponding id to closed. Throws an
     * exception if the data query process already had the closed state or if the corresponding
     * data query process does not exist.
     *
     * @param ctx the transaction context.
     * @param id  the unique id of the data query.
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void Close(Context ctx, String id) {
        ChaincodeStub stub = retrieveStub(ctx, id);

        DataQuery dataQuery = DataQuery.deserialize(stub.getState(id));
        if (dataQuery.isClosed())
            throw new ChaincodeException(String.format("Data query, %s, is already closed", id));

        dataQuery.setClosed();
        byte[] serDataQuery = DataQuery.serialize(dataQuery);
        stub.putState(id, serDataQuery);
    }

    /**
     * The data query process corresponding to the id is retrieved from the world state. Throws
     * an exception if the corresponding data query process does not exist.
     *
     * @param ctx the transaction context.
     * @param id  the unique id of the data query.
     * @return the data query.
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public byte[] Retrieve(Context ctx, String id) {
        ChaincodeStub stub = retrieveStub(ctx, id);
        return stub.getState(id);
    }

    /**
     * The data query corresponding to the id is removed from the world state and the data query
     * is returned. The event RemoveQuery is also set in the transaction. Throws an exception
     * if the corresponding data query process does not exist.
     *
     * @param ctx the transaction context.
     * @param id  the unique id of the data query.
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public byte[] Remove(Context ctx, String id) {
        ChaincodeStub stub = retrieveStub(ctx, id);
        byte[] serDataQuery = stub.getState(id);
        stub.delState(id);
        stub.setEvent("RemoveQuery", serDataQuery);
        return serDataQuery;
    }

    /**
     * Checks the existence of the data query on the ledger.
     *
     * @param ctx the transaction context.
     * @param id  the unique id of the data query.
     * @return boolean indicating the existence of the data query.
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean Exists(Context ctx, final String id) {
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
        if (!Exists(ctx, key))
            throw new ChaincodeException(String.format("Data query, %s, does not exist", key));

        return stub;
    }

}