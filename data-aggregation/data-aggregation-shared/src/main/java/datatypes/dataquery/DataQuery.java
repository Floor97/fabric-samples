package datatypes.dataquery;

import datatypes.values.IPFSConnection;
import datatypes.values.IPFSFile;
import io.ipfs.multihash.Multihash;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import org.json.JSONObject;

import static java.nio.charset.StandardCharsets.UTF_8;

@DataType()
public class DataQuery {

    @Property()
    private final String id;

    @Property()
    private final DataQuerySettings settings;

    @Property()
    private final IPFSFile ipfsFile;

    @Property()
    private DataQueryState state = DataQueryState.WAITING;

    @Property()
    private boolean incFlag = false;

    public DataQuery(String id, DataQuerySettings settings, IPFSFile ipfsFile) {
        this.id = id;
        this.settings = settings;
        this.ipfsFile = ipfsFile;
    }

    public DataQuery(String id, DataQuerySettings settings, Multihash hash) {
        this.id = id;
        this.settings = settings;
        this.ipfsFile = IPFSConnection.getInstance().getFile(hash);
    }

    public String getId() {
        return id;
    }

    public DataQuerySettings getSettings() {
        return settings;
    }

    public IPFSFile getIpfsFile() {
        return ipfsFile;
    }

    public boolean isWaiting() {
        return (state == DataQueryState.WAITING);
    }

    public boolean isDone() {
        return (state == DataQueryState.DONE);
    }

    public boolean isClosed() {
        return (state == DataQueryState.CLOSED);
    }

    public DataQuery setWaiting() {
        this.state = DataQueryState.WAITING;
        return this;
    }

    public DataQuery setDone() {
        this.state = DataQueryState.DONE;
        return this;
    }

    public DataQuery setClosed() {
        this.state = DataQueryState.CLOSED;
        return this;
    }

    public boolean isIncFlag() {
        return incFlag;
    }

    public void setIncFlag() {
        this.incFlag = true;
    }

    /**
     * Deserializes the JSON into a DataQuery object.
     *
     * @param data the JSON.
     * @return the DataQuery object.
     */
    public static DataQuery deserialize(byte[] data) {
        JSONObject json = new JSONObject(new String(data, UTF_8));

        String id = json.getString("id");
        int nrOperators = json.getInt("nrOperators");
        long endTime = json.getLong("duration");
        boolean incFlag = json.getBoolean("incFlag");
        IPFSFile file = IPFSFile.deserialize(json.getString("file"), nrOperators);

        DataQuery dataQuery = new DataQuery(id, new DataQuerySettings(nrOperators, endTime), file);
        if (incFlag) dataQuery.setIncFlag();
        dataQuery.state = json.getEnum(DataQueryState.class, "state");

        return dataQuery;
    }

    /**
     * Serializes the DataQuery object into JSON.
     *
     * @param dataQuery the aggregation process.
     * @return the JSON value of the data query object.
     */
    public static byte[] serialize(DataQuery dataQuery) {
        JSONObject json = new JSONObject()
                .put("id", dataQuery.id)
                .put("nrOperators", dataQuery.getSettings().getNrOperators())
                .put("duration", dataQuery.getSettings().getDuration())
                .put("state", dataQuery.state)
                .put("incFlag", dataQuery.incFlag)
                .put("file", IPFSFile.serialize(dataQuery.ipfsFile));
        return json.toString().getBytes(UTF_8);
    }

    @Override
    public String toString() {
        return "queryID: " + this.id +
                ",\nsettings:\n " + this.settings +
                ",\nprocess data: " + this.ipfsFile +
                ",\n state: " + this.state;
    }

    public enum DataQueryState {
        WAITING, DONE, CLOSED;

        @Override
        public String toString() {
            if (this == DataQueryState.WAITING) return "Waiting";
            if (this == DataQueryState.DONE) return "Done";
            else return "Closed";
        }
    }
}