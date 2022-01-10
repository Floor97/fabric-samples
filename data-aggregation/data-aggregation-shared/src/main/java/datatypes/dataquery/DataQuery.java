package datatypes.dataquery;

import datatypes.values.IPFSFile;
import io.ipfs.multihash.Multihash;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import org.json.JSONObject;

import static java.nio.charset.StandardCharsets.UTF_8;

@DataType()
public class DataQuery {

    @Property()
    private String id;

    @Property()
    private DataQuerySettings settings;

    @Property()
    private boolean incFlag;

    @Property()
    private IPFSFile ipfsFile;

    @Property()
    private DataQueryState state = DataQueryState.WAITING;

    public String getId() {
        return id;
    }

    public DataQuery setId(String id) {
        this.id = id;
        return this;
    }

    public DataQuerySettings getSettings() {
        return settings;
    }

    public DataQuery setSettings(DataQuerySettings settings) {
        this.settings = settings;
        return this;
    }

    public IPFSFile getIpfsFile() {
        return ipfsFile;
    }

    public DataQuery setIpfsFile(IPFSFile ipfsFile) {
        this.ipfsFile = ipfsFile;
        return this;
    }

    public boolean isIncFlag() {
        return incFlag;
    }

    public void setIncFlag() {
        this.incFlag = true;
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

    public static DataQuery deserialize(byte[] data) {
        JSONObject json = new JSONObject(new String(data, UTF_8));

        String id = json.getString("id");
        int nrOperators = json.getInt("nrOperators");
        long endTime = json.getLong("duration");
        boolean incFlag = json.getBoolean("incFlag");
        IPFSFile file = IPFSFile.deserialize(json.getString("file"));

        DataQuery dataQuery = createInstance(id,
                DataQuerySettings.createInstance(nrOperators, endTime),
                file
        );
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

    /**
     * Factory method for creating an DataQuery object.
     *
     * @param id       the unique id of the process.
     * @param settings The settings of the process.
     * @param hash     The hash of the IPFS file used in the process.
     * @return the created DataQuery object.
     */
    public static DataQuery createInstance(String id, DataQuerySettings settings, Multihash hash) {
        return new DataQuery()
                .setId(id)
                .setSettings(settings)
                .setIpfsFile(new IPFSFile(hash));
    }

    /**
     * Factory method for creating an DataQuery object.
     *
     * @param id       the unique id of the process.
     * @param settings The settings of the process.
     * @param file     the IPFSFile used in the process.
     * @return the created DataQuery object.
     */
    public static DataQuery createInstance(String id, DataQuerySettings settings, IPFSFile file) {
        return new DataQuery()
                .setId(id)
                .setSettings(settings)
                .setIpfsFile(file);
    }

    @Override
    public String toString() {
        return "queryID: " + this.id +
                ",\nsettings:\n " + this.settings +
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