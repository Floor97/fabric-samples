package datatypes.aggregationprocess;

import datatypes.values.EncryptedData;
import datatypes.values.EncryptedNonces;
import shared.Pair;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class AggregationProcessData {

    private EncryptedData cipherData;
    private ArrayList<EncryptedNonces> cipherNonces;
    private int nrParticipants = 0;
    private final int nrOperators;

    private AggregationProcessData(int nrOperators) {
       this.nrOperators = nrOperators;
    }

    public EncryptedData getCipherData() {
        return cipherData;
    }

    public AggregationProcessData setCipherData(EncryptedData cipherData) {
        this.cipherData = cipherData;
        return this;
    }

    public ArrayList<EncryptedNonces> getCipherNonces() {
        return cipherNonces;
    }

    public AggregationProcessData setCipherNonces(ArrayList<EncryptedNonces> cipherNonces) {
        this.cipherNonces = cipherNonces;
        this.nrParticipants = cipherNonces.size();
        return this;
    }

    public AggregationProcessData addNonces(EncryptedNonces cipherNonce) {
        if(cipherNonce.getNonces().length != this.nrOperators) throw new RuntimeException("The amount of nonces supplied is not equal to the amount of operators involved");
        this.cipherNonces.add(cipherNonce);
        nrParticipants++;
        return this;
    }

    public int getNrParticipants() {
        return this.nrParticipants;
    }

    public int getNrOperators() {return this.nrOperators;}

    /**
     * Factory method of AggregationProcessData.
     * @param cipherData the data that is aggregated.
     * @param nrOperators the number of operators involved in the aggregation process.
     * @return the AggregationProcessData object.
     */
    public static AggregationProcessData createInstance(EncryptedData cipherData, int nrOperators) {
        return new AggregationProcessData(nrOperators)
                .setCipherData(cipherData)
                .setCipherNonces(new ArrayList<>());
    }

    @Override
    public String toString() {
        if(this.cipherData == null)
            return "    ciphertext of data: null,\n     " +
                    "exponent of data: null,\n    " +
                    "number of participants: " + this.nrParticipants +
                    ",\n    ciphertext of nonces: " + this.cipherNonces.stream().map(EncryptedNonces::toString).collect(Collectors.joining());


        return "    ciphertext of data: " + this.cipherData.getData() +
                ",\n    exponent of data: " + this.cipherData.getExponent() +
                ",\n    number of participants: " + this.nrParticipants +
                ",\n    ciphertext of nonces: " + this.cipherNonces.stream().map(EncryptedNonces::toString).collect(Collectors.joining());
    }
}
