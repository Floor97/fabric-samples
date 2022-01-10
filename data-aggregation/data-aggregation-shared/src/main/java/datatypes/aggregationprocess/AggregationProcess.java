package datatypes.aggregationprocess;

import datatypes.values.IPFSFile;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import org.json.JSONObject;

import static java.nio.charset.StandardCharsets.UTF_8;

@DataType()
public class AggregationProcess {

    @Property()
    private String id;

    @Property()
    private IPFSFile ipfsFile;

    @Property()
    private AggregationProcessState state = AggregationProcessState.SELECTING;


    public String getId() {
        return id;
    }

    public AggregationProcess setId(String processID) {
        this.id = processID;
        return this;
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

    public IPFSFile getIpfsFile() {
        return ipfsFile;
    }

    public AggregationProcess setIpfsFile(IPFSFile ipfsFile) {
        this.ipfsFile = ipfsFile;
        return this;
    }

    //todo make nrParticipants and nonces less easy to manipulate

    /**
     * Deserializes the JSON into an AggregationProcess object.
     *
     * @param data the JSON.
     * @return the AggregationProcess object.
     */
    public static AggregationProcess deserialize(byte[] data) {
        JSONObject json = new JSONObject(new String(data, UTF_8));

        String id = json.getString("id");
        IPFSFile ipfsFile = IPFSFile.deserialize(json.getString("file"));
        AggregationProcessState state = json.getEnum(AggregationProcessState.class, "state");

        AggregationProcess aggregationProcess = createInstance(id, ipfsFile);
        aggregationProcess.state = state;
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

    /**
     * Factory method for creating an AggregationProcess object.
     *
     * @param id   the unique id of the aggregation process.
     * @param file the file corresponding to the process.
     * @return the created AggregationProcess object.
     */
    public static AggregationProcess createInstance(String id, IPFSFile file) {
        return new AggregationProcess().setId(id).setIpfsFile(file);
    }

    @Override
    public String toString() {
        return "id: " + this.id +
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
