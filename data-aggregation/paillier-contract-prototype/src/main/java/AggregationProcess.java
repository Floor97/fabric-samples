import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import java.io.IOException;
import java.math.BigInteger;

@JsonPropertyOrder({"key","modulus","cData", "expData","nrParticipants","status"})
@JsonIgnoreProperties({"aggregating", "closed", "om"})
@DataType()
public class AggregationProcess {
    private static final String AGGREGATING = "AGGREGATING";
    private static final String CLOSED = "CLOSED";
    private static final ObjectMapper om = new ObjectMapper();

    @Property()
    private String key;

    @Property()
    private BigInteger modulus;

    @Property()
    private BigInteger cData;

    @Property()
    private int expData;

    @Property()
    private int nrParticipants;

    @Property()
    private String status;



    public String getKey() {
        return key;
    }

    public AggregationProcess setKey(String processID) {
        this.key = processID;
        return this;
    }

    public BigInteger getModulus() {
        return modulus;
    }

    public AggregationProcess setModulus(BigInteger modulus) {
        this.modulus = modulus;
        return this;
    }

    public BigInteger getcData() {
        return cData;
    }

    public AggregationProcess setcData(BigInteger cData) {
        this.cData = cData;
        return this;
    }

    public int getExpData() {
        return expData;
    }

    public AggregationProcess setExpData(int expData) {
        this.expData = expData;
        return this;
    }

    public int getNrParticipants() {
        return nrParticipants;
    }

    public AggregationProcess setNrParticipants(int nrParticipants) {
        this.nrParticipants = nrParticipants;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public AggregationProcess setStatus(String status) {
        this.status = status;
        return this;
    }

    public boolean isAggregating() {
        return this.status.equals(AggregationProcess.AGGREGATING);
    }

    public boolean isClosed() {
        return this.status.equals(AggregationProcess.CLOSED);
    }

    public AggregationProcess setAggregating() {
        this.status = AggregationProcess.AGGREGATING;
        return this;
    }

    public AggregationProcess setClosed() {
        this.status = AggregationProcess.CLOSED;
        return this;
    }

    /**
     * Deserializes the JSON into an AggregationProcess object.
     * @param data the JSON.
     * @return the AggregationProcess object.
     */
    public static AggregationProcess deserialize(String data) {
        AggregationProcess aggregationProcess = null;
        try {
            aggregationProcess = om.readValue(data, AggregationProcess.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return aggregationProcess;
    }

    /**
     * Serializes the AggregationProcess object into ordered JSON.
     * @param aggregationProcess the aggregation process.
     * @return the JSON value of the aggregation process object.
     */
    public static String serialize(AggregationProcess aggregationProcess) {
        String serAggregationProcess = null;
        try {
            serAggregationProcess = om.writeValueAsString(aggregationProcess);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return serAggregationProcess;
    }

    /**
     * Factory method for creating an AggregationProcess object.
     * @param key the unique key of the aggregation process.
     * @param modulus the modulus of the public key that encrypts the cData.
     * @param cData the ciphertext of the encrypted aggregated data.
     * @param expData the exponent of the ciphertext of the aggregated data.
     * @param nrParticipants the number of participants in the aggregation process.
     * @param status the status of the aggregation process.
     * @return the created AggregationProcess object.
     */
    public static AggregationProcess createInstance(String key, BigInteger modulus, BigInteger cData,
                                                    int expData, int nrParticipants, String status) {
        return new AggregationProcess().setKey(key).setModulus(modulus).setcData(cData).setExpData(expData)
                .setNrParticipants(nrParticipants).setStatus(status);
    }

    @Override
    public String toString() {
        StringBuilder build = new StringBuilder();
        build.append("key: ").append(this.key)
                .append(", modulus: ").append(this.modulus)
                .append(", cData: ").append(this.cData)
                .append(", expData: ").append(this.cData)
                .append(", nrParticipants: ").append(this.nrParticipants)
                .append(", status: ").append(this.status);
        return build.toString();
    }
}
