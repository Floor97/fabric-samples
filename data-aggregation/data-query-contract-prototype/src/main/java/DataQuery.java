import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import java.io.IOException;

@JsonPropertyOrder({"queryID","modulus","result", "expResult","timeLimit","nrParticipants","status"})
@JsonIgnoreProperties({"waiting", "done", "closed", "om"})
@DataType()
public class DataQuery {
    private static final String WAITING = "WAITING";
    private static final String DONE = "DONE";
    private static final String CLOSED = "CLOSED";
    private static final ObjectMapper om = new ObjectMapper();

    @Property()
    private String queryID;

    @Property()
    private String modulus;

    @Property()
    private String result;

    @Property()
    private String expResult;

    @Property()
    private String timeLimit;

    @Property()
    private String nrParticipants;

    @Property()
    private String status;



    public String getQueryID() {
        return queryID;
    }

    public DataQuery setQueryID(String processID) {
        this.queryID = processID;
        return this;
    }

    public String getModulus() {
        return modulus;
    }

    public DataQuery setModulus(String modulus) {
        this.modulus = modulus;
        return this;
    }

    public String getResult() {
        return result;
    }

    public DataQuery setResult(String result) {
        this.result = result;
        return this;
    }

    public String getExpResult() {
        return expResult;
    }

    public DataQuery setExpResult(String expResult) {
        this.expResult = expResult;
        return this;
    }

    public String getTimeLimit() {
        return timeLimit;
    }

    public DataQuery setTimeLimit(String timeLimit) {
        this.timeLimit = timeLimit;
        return this;
    }

    public String getNrParticipants() {
        return nrParticipants;
    }

    public DataQuery setNrParticipants(String nrParticipants) {
        this.nrParticipants = nrParticipants;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public DataQuery setStatus(String status) {
        this.status = status;
        return this;
    }

    public boolean isWaiting() {
        return this.status.equals(DataQuery.WAITING);
    }

    public boolean isDone() {
        return this.status.equals(DataQuery.DONE);
    }

    public boolean isClosed() {
        return this.status.equals(DataQuery.CLOSED);
    }

    public DataQuery setWaiting() {
        this.status = DataQuery.WAITING;
        return this;
    }

    public DataQuery setDone() {
        this.status = DataQuery.DONE;
        return this;
    }

    public DataQuery setClosed() {
        this.status = DataQuery.CLOSED;
        return this;
    }

    /**
     * Deserializes the JSON into an DataQuery object.
     * @param data the JSON.
     * @return the DataQuery object.
     */
    public static DataQuery deserialize(String data) {
        DataQuery dataQuery = null;
        try {
            dataQuery = om.readValue(data, DataQuery.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataQuery;
    }

    /**
     * Serializes the DataQuery object into ordered JSON.
     * @param dataQuery the data query.
     * @return the JSON value of the DataQuery object.
     */
    public static String serialize(DataQuery dataQuery) {
        String serDataQuery = null;
        try {
            serDataQuery = om.writeValueAsString(dataQuery);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return serDataQuery;
    }

    /**
     * Factory method for creating an DataQuery object.
     * @param queryID the unique ID of the data query.
     * @param modulus the modulus of the public key that encrypts the result.
     * @param result the ciphertext of the result.
     * @param expResult the exponent of the ciphertext of the result.
     * @param timeLimit the time limit of the data query.
     * @param nrParticipants the number of participants in the data query.
     * @param status the status of the data query.
     * @return the created DataQuery object.
     */
    public static DataQuery createInstance(String queryID, String modulus, String result,
                                           String expResult, String timeLimit, String nrParticipants, String status) {
        return new DataQuery()
                .setQueryID(queryID)
                .setModulus(modulus)
                .setResult(result)
                .setExpResult(expResult)
                .setTimeLimit(timeLimit)
                .setNrParticipants(nrParticipants)
                .setStatus(status);
    }

    @Override
    public String toString() {
        return "queryID: " + this.queryID +
                ", modulus: " + this.modulus +
                ", result: " + this.result +
                ", expResult: " + this.result +
                ", timeLimit: " + this.timeLimit +
                ", nrParticipants: " + this.nrParticipants +
                ", status: " + this.status;
    }
}