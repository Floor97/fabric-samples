package datatypes.aggregationprocess;

import datatypes.values.AggregationIPFSFile;
import datatypes.values.IPFSConnection;
import datatypes.values.IPFSFile;
import io.ipfs.multihash.Multihash;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import org.json.JSONObject;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

@DataType()
public class AggregationProcess {

    @Property()
    private final String id;

    @Property()
    private final AggregationIPFSFile ipfsFile;

    @Property()
    private AggregationProcessState state = AggregationProcessState.SELECTING;

    public AggregationProcess(String id, AggregationIPFSFile ipfsFile) {
        this.id = id;
        this.ipfsFile = ipfsFile;
    }

    public AggregationProcess(String id, Multihash hash) throws IOException {
        this.id = id;
        this.ipfsFile = IPFSConnection.getInstance().getAggregationIPFSFile(hash);
    }

    public String getId() {
        return id;
    }

    public AggregationIPFSFile getIpfsFile() {
        return ipfsFile;
    }

    public boolean isSelecting() {
        return this.state == AggregationProcessState.SELECTING;
    }

    public boolean isAggregating() {
        return this.state == AggregationProcessState.AGGREGATING;
    }

    public boolean isClosed() {
        return this.state == AggregationProcessState.CLOSED;
    }

    public AggregationProcess setAggregating() {
        this.state = AggregationProcessState.AGGREGATING;
        return this;
    }

    public AggregationProcess setClosed() {
        this.state = AggregationProcessState.CLOSED;
        return this;
    }

    /**
     * Deserializes the JSON into an AggregationProcess object.
     *
     * @param data the JSON.
     * @return the AggregationProcess object.
     */
    public static AggregationProcess deserialize(byte[] data) {
        JSONObject json = new JSONObject(new String(data, UTF_8));

        String id = json.getString("id");
        AggregationIPFSFile ipfsFile = AggregationIPFSFile.deserialize(json.getString("file"));

        AggregationProcess aggregationProcess = new AggregationProcess(id, ipfsFile);
        aggregationProcess.state = json.getEnum(AggregationProcessState.class, "state");

        return aggregationProcess;
    }

    /**
     * Serializes the AggregationProcess object into JSON.
     *
     * @param aggregationProcess the aggregation process.
     * @return the JSON value of the aggregation process object.
     */
    public static String serialize(AggregationProcess aggregationProcess) {
        JSONObject json = new JSONObject()
                .put("id", aggregationProcess.id)
                .put("file", aggregationProcess.ipfsFile.serialize())
                .put("state", aggregationProcess.state);
        return json.toString();
    }

    @Override
    public String toString() {
        return "id: " + this.id +
                "process data: " + this.ipfsFile.toString() +
                "state: " + this.state;
    }


    public enum AggregationProcessState {
        SELECTING, AGGREGATING, CLOSED;

        @Override
        public String toString() {
            if (this == AggregationProcessState.SELECTING) return "Selecting";
            if (this == AggregationProcessState.AGGREGATING) return "Aggregating";
            else return "Closed";
        }
    }
}
