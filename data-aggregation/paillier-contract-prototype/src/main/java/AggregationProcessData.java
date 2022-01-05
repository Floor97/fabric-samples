import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class AggregationProcessData {

    private Pair<BigInteger, Integer> cipherData;
    private ArrayList<String[]> cipherNonces;
    private int nrParticipants = 0;
    private final int nrOperators;

    private AggregationProcessData(int nrOperators) {
       this.nrOperators = nrOperators;
    }

    public Pair<BigInteger, Integer> getCipherData() {
        return cipherData;
    }

    public AggregationProcessData setCipherData(Pair<BigInteger, Integer> cipherData) {
        this.cipherData = cipherData;
        return this;
    }

    public ArrayList<String[]> getCipherNonces() {
        return cipherNonces;
    }

    public AggregationProcessData setCipherNonces(ArrayList<String[]> cipherNonces) {
        this.cipherNonces = cipherNonces;
        this.nrParticipants = cipherNonces.size();
        return this;
    }

    public AggregationProcessData addNonce(String[] cipherNonce) {
        if(cipherNonce.length != this.nrOperators) throw new RuntimeException("The amount of nonces supplied is not equal to the amount of operators involved");
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
    public static AggregationProcessData createInstance(Pair<BigInteger, Integer> cipherData, int nrOperators) {
        return new AggregationProcessData(nrOperators)
                .setCipherData(cipherData)
                .setCipherNonces(new ArrayList<>());
    }

    @Override
    public String toString() {
        return "    ciphertext of data: " + this.cipherData.getP1().intValue() +
                ",\n    exponent of data: " + this.cipherData.getP2() +
                ",\n    number of participants: " + this.nrParticipants +
                ",\n    ciphertext of nonces: " + this.cipherNonces.stream().map(Arrays::toString).collect(Collectors.joining());
    }
}
