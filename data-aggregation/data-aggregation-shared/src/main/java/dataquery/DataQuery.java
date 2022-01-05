package dataquery;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import org.json.JSONObject;
import shared.Pair;

import static java.nio.charset.StandardCharsets.UTF_8;

@JsonPropertyOrder({"id", "settings", "result", "state"})
@JsonIgnoreProperties({"waiting", "done", "closed"})
@DataType()
public class DataQuery {

    @Property()
    private String id;

    @Property()
    private DataQuerySettings settings;

    @Property()
    private DataQueryResult result;

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

    public DataQueryResult getResult() {
        return result;
    }

    public DataQuery setResult(DataQueryResult result) {
        this.result = result;
        return this;
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
        String paillierModulus = json.getString("paillierModulus");
        String postQuantumPk = json.getString("postQuantumPk");
        int nrOperators = json.getInt("nrOperators");
        long endTime = json.getLong("endTime");

        DataQuery dataQuery;
        if (!json.has("result")) {
            String cipherData = json.getString("cipherData");
            String exponent = json.getString("exponent");
            int nrParticipants = json.getInt("nrParticipants");
            String[] nonces = json.getJSONArray("cipherNonces").toList().toArray(new String[0]);
            boolean incFlag = json.getBoolean("incFlag");

            dataQuery = createInstance(id,
                    DataQuerySettings.createInstance(paillierModulus, postQuantumPk, nrOperators, endTime),
                    DataQueryResult.createInstance(new Pair<>(cipherData, exponent), nonces, nrParticipants)
            );
            if(incFlag) dataQuery.getResult().setIncFlag();
        } else {
            dataQuery = createInstance(id,
                    DataQuerySettings.createInstance(paillierModulus, postQuantumPk, nrOperators, endTime),
                    null
            );
        }

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
        DataQueryResult res = dataQuery.getResult();

        JSONObject json = new JSONObject()
                .put("id", dataQuery.id)
                .put("paillierModulus", dataQuery.getSettings().getPaillierModulus())
                .put("postQuantumPk", dataQuery.getSettings().getPostQuantumPk())
                .put("nrOperators", dataQuery.getSettings().getNrOperators())
                .put("endTime", dataQuery.getSettings().getEndTime())
                .put("state", dataQuery.state);
        if (res != null)
            json.put("cipherData", res.getCipherData().getP1())
                    .put("exponent", res.getCipherData().getP2())
                    .put("cipherNonces", dataQuery.getResult().getCipherNonces())
                    .put("nrParticipants", dataQuery.getResult().getNrParticipants())
                    .put("incFlag", dataQuery.getResult().isIncFlag());
        else json.put("result", "null");
        return json.toString().getBytes(UTF_8);
    }

    /**
     * Factory method for creating an DataQuery object.
     *
     * @param id       the unique id of the process.
     * @param settings The settings of the process.
     * @param result   The result of the process.
     * @return the created DataQuery object.
     */
    public static DataQuery createInstance(String id, DataQuerySettings settings, DataQueryResult result) {
        return new DataQuery()
                .setId(id)
                .setSettings(settings)
                .setResult(result);
    }

    @Override
    public String toString() {
        return "queryID: " + this.id +
                ",\n settings: " + this.settings +
                ",\n result: " + this.result +
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