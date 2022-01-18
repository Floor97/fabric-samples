package datatypes.aggregationprocess;

import applications.operator.AggregationIPFSFile;
import datatypes.values.IPFSConnection;
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
    private int nrOperatorsSelected;

    @Property()
    private AggregationProcessState state = AggregationProcessState.SELECTING;

    public AggregationProcess(String id, AggregationIPFSFile ipfsFile, int nrOperatorsSelected) {
        this.id = id;
        this.ipfsFile = ipfsFile;
        this.nrOperatorsSelected = nrOperatorsSelected;
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

    /**
     * Deserializes the JSON into an AggregationProcess object.
     *
     * @param data the JSON.
     * @return the AggregationProcess object.
     */
    public static AggregationProcess deserialize(byte[] data) throws IOException {
        JSONObject json = new JSONObject(new String(data, UTF_8));

        String id = json.getString("id");
        AggregationIPFSFile ipfsFile = IPFSConnection.getInstance().getAggregationIPFSFile(Multihash.fromHex(json.getString("hash")));

        AggregationProcess aggregationProcess = new AggregationProcess(id, ipfsFile, json.getInt("nrOperatorsSelected"));
        aggregationProcess.state = json.getEnum(AggregationProcessState.class, "state");

        return aggregationProcess;
    }

    /**
     * Serializes the AggregationProcess object into JSON.
     *
     * @return the JSON value of the aggregation process object.
     */
    public String serialize() throws IOException {
        JSONObject json = new JSONObject()
                .put("id", this.id)
                .put("hash", this.ipfsFile.getHash().toHex())
                .put("nrOperatorsSelected", this.nrOperatorsSelected)
                .put("state", this.state);
        return json.toString();
    }

    @Override
    public String toString() {
        return "id: " + this.id +
                "process data: " + this.ipfsFile.toString() +
                "number of selected operators: " + this.nrOperatorsSelected +
                "state: " + this.state;
    }

    public String getId() {
        return id;
    }

    public AggregationIPFSFile getIpfsFile() {
        return ipfsFile;
    }

    public int getNrOperatorsSelected() {
        return nrOperatorsSelected;
    }

    public void addOperator() {
        this.nrOperatorsSelected++;
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
}
