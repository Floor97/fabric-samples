package datatypes.dataquery;

import applications.asker.DataQueryIPFSFile;
import datatypes.values.IPFSConnection;
import io.ipfs.multihash.Multihash;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import org.json.JSONObject;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

@DataType()
public class DataQuery {

    @Property()
    private final String id;

    @Property()
    private final DataQuerySettings settings;

    @Property()
    private final DataQueryIPFSFile ipfsFile;

    @Property()
    private int nrParticipants;

    @Property()
    private DataQueryState state = DataQueryState.WAITING;

    @Property()
    private boolean incFlag = false;

    public DataQuery(String id, DataQuerySettings settings, DataQueryIPFSFile ipfsFile, int nrParticipants) {
        this.id = id;
        this.settings = settings;
        this.ipfsFile = ipfsFile;
        this.nrParticipants = nrParticipants;
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

    /**
     * Deserializes the JSON into a DataQuery object.
     *
     * @param data the JSON.
     * @return the DataQuery object.
     */
    public static DataQuery deserialize(byte[] data) throws IOException {
        JSONObject json = new JSONObject(new String(data, UTF_8));

        String id = json.getString("id");
        int nrOperators = json.getInt("nrOperators");
        long endTime = json.getLong("duration");
        int nrParticipants = json.getInt("nrParticipants");
        int nrExpectedParticipants = json.getInt("nrExpectedParticipants");
        boolean incFlag = json.getBoolean("incFlag");
        DataQueryIPFSFile file = IPFSConnection.getInstance().getDataQueryIPFSFile(Multihash.fromHex(json.getString("hash")));

        DataQuery dataQuery = new DataQuery(id, new DataQuerySettings(nrOperators, nrExpectedParticipants, endTime), file, nrParticipants);

        if (incFlag) dataQuery.setIncFlag();
        dataQuery.state = json.getEnum(DataQueryState.class, "state");

        return dataQuery;
    }

    /**
     * Serializes the DataQuery object into JSON.
     *
     * @return the JSON value of the data query object.
     */
    public String serialize() throws IOException {
        JSONObject json = new JSONObject()
                .put("id", this.id)
                .put("nrOperators", this.getSettings().getNrOperators())
                .put("duration", this.getSettings().getDuration())
                .put("nrParticipants", this.nrParticipants)
                .put("nrExpectedParticipants", this.getSettings().getNrExpectedParticipants())
                .put("state", this.state)
                .put("incFlag", this.incFlag)
                .put("hash", this.ipfsFile.getHash().toHex());
        return json.toString();
    }

    @Override
    public String toString() {
        return "queryID: " + this.id +
                ",\nsettings:\n " + this.settings +
                ",\nprocess data: " + this.ipfsFile +
                ",\n state: " + this.state;
    }

    public String getId() {
        return id;
    }

    public DataQuerySettings getSettings() {
        return settings;
    }

    public int getNrParticipants() {
        return nrParticipants;
    }

    public void setNrParticipants(int nrParticipants) {
        if (this.nrParticipants == -1)
            this.nrParticipants = nrParticipants;
    }

    public DataQueryIPFSFile getIpfsFile() {
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

}