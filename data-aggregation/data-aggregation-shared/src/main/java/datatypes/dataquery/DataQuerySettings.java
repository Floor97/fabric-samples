package datatypes.dataquery;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DataQuerySettings {

    private String paillierModulus;
    private String postQuantumPk;
    private int nrOperators;
    private long endTime;

    public String getPaillierModulus() {
        return paillierModulus;
    }

    public DataQuerySettings setPaillierModulus(String paillierModulus) {
        this.paillierModulus = paillierModulus;
        return this;
    }

    public String getPostQuantumPk() {
        return postQuantumPk;
    }

    public DataQuerySettings setPostQuantumPk(String postQuantumPk) {
        this.postQuantumPk = postQuantumPk;
        return this;
    }

    public int getNrOperators() {
        return nrOperators;
    }

    public DataQuerySettings setNrOperators(int nrOperators) {
        this.nrOperators = nrOperators;
        return this;
    }

    public long getEndTime() {
        return endTime;
    }

    public DataQuerySettings setEndTime(long endTime) {
        this.endTime = endTime;
        return this;
    }

    /**
     * Factory method for DataQuerySettings.
     * @param paillierModulus the modulus of the public key of the paillier encryption scheme.
     * @param postQuantumPk the public key of a post-quantum encryption scheme.
     * @param nrOperators the number of operators used in the process.
     * @param endTime the end time of the process.
     * @return a new DataQuerySettings object.
     */
    public static DataQuerySettings createInstance(String paillierModulus, String postQuantumPk, int nrOperators, long endTime) {
        return new DataQuerySettings()
                .setPaillierModulus(paillierModulus)
                .setPostQuantumPk(postQuantumPk)
                .setNrOperators(nrOperators)
                .setEndTime(endTime);
    }

    @Override
    public String toString() {
        return "    paillier modulus: " + this.paillierModulus
                + ",\n    post-quantum public key: " + this.postQuantumPk
                + ",\n    number of operators: " + this.nrOperators
                + ",\n    end time: " + new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date(this.endTime));
    }
}
