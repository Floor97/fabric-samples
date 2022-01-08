package datatypes.aggregationprocess;

import datatypes.values.EncryptedData;
import datatypes.values.EncryptedNonces;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import org.json.JSONObject;

import java.util.ArrayList;

import static java.nio.charset.StandardCharsets.UTF_8;

@DataType()
public class AggregationProcess {

    @Property()
    private String id;

    @Property()
    private AggregationProcessKeys keystore;

    @Property()
    private AggregationProcessData data;

    @Property()
    private AggregationProcessState state = AggregationProcessState.SELECTING;


    public String getId() {
        return id;
    }

    public AggregationProcess setId(String processID) {
        this.id = processID;
        return this;
    }

    public AggregationProcessKeys getKeystore() {
        return keystore;
    }

    public AggregationProcess setKeystore(AggregationProcessKeys keystore) {
        this.keystore = keystore;
        return this;
    }

    public AggregationProcessData getData() {
        return data;
    }

    public AggregationProcess setData(AggregationProcessData data) {
        this.data = data;
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
        String paillierModulus = json.getString("paillierModulus");
        String[] operatorKeys = json.getJSONArray("operatorKeys").toList().toArray(new String[0]);
        String cipherData = json.getString("cipherData");
        String exponent = json.getString("exponent");
        ArrayList<String> cipherNonces = new ArrayList(json.getJSONArray("cipherNonces").toList());

        ArrayList<EncryptedNonces> nonces = new ArrayList<>(cipherNonces.size());
        for(String nonce : cipherNonces)
            nonces.add(EncryptedNonces.deserialize(nonce));

        int nrOperators = json.getInt("nrOperators");
        AggregationProcessState state = json.getEnum(AggregationProcessState.class, "state");


        AggregationProcess aggregationProcess = createInstance(id,
                AggregationProcessKeys.createInstance(paillierModulus, nrOperators).setOperatorKeys(operatorKeys),
                AggregationProcessData.createInstance(cipherData.equals("null") ? null : new EncryptedData(cipherData, exponent), nrOperators)
                        .setCipherNonces(nonces)
        );
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
        EncryptedData data = aggregationProcess.getData().getCipherData();
        ArrayList<String> serNonces = new ArrayList<>();
        for(EncryptedNonces nonces : aggregationProcess.getData().getCipherNonces())
            serNonces.add(EncryptedNonces.serialize(nonces));

        return new JSONObject()
                .put("id", aggregationProcess.id)
                .put("paillierModulus", aggregationProcess.keystore.getPaillierModulus())
                .put("operatorKeys", aggregationProcess.getKeystore().getOperatorKeys())
                .put("cipherData", data == null ? "null" : data.getData())
                .put("exponent", data == null ? "null" : String.valueOf(data.getExponent()))
                .put("cipherNonces", serNonces)
                .put("nrOperators", aggregationProcess.getData().getNrOperators())
                .put("state", aggregationProcess.state)
                .toString().getBytes(UTF_8);
    }

    /**
     * Factory method for creating an AggregationProcess object.
     *
     * @param id       the unique id of the aggregation process.
     * @param keystore an object that holds the keys involved in the aggregation process.
     * @param data     an object that holds the data collected in the aggregation process.
     * @return the created AggregationProcess object.
     */
    public static AggregationProcess createInstance(String id, AggregationProcessKeys keystore, AggregationProcessData data) {
        return new AggregationProcess().setId(id).setKeystore(keystore).setData(data);
    }

    @Override
    public String toString() {
        return "id: " + this.id +
                "keystore: " + this.keystore +
                "data: " + this.data +
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
