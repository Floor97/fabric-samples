package datatypes.aggregationprocess;

import datatypes.values.IPFSConnection;
import datatypes.values.IPFSFile;
import io.ipfs.multihash.Multihash;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import org.json.JSONObject;

import static java.nio.charset.StandardCharsets.UTF_8;

@DataType()
public class AggregationProcess {

    @Property()
    private final String id;

    @Property()
    private final IPFSFile ipfsFile;

    @Property()
    private AggregationProcessState state = AggregationProcessState.SELECTING;

    public AggregationProcess(String id, IPFSFile ipfsFile) {
        this.id = id;
        this.ipfsFile = ipfsFile;
    }

    public AggregationProcess(String id, Multihash hash) {
        this.id = id;
        this.ipfsFile = IPFSConnection.getInstance().getFile(hash);
    }

    public String getId() {
        return id;
    }

    public IPFSFile getIpfsFile() {
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
        IPFSFile ipfsFile = IPFSFile.deserialize(json.getString("file"), -1);

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
    public static byte[] serialize(AggregationProcess aggregationProcess) {
        return new JSONObject()
                .put("id", aggregationProcess.id)
                .put("file", aggregationProcess.ipfsFile)
                .put("state", aggregationProcess.state)
                .toString().getBytes(UTF_8);
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
