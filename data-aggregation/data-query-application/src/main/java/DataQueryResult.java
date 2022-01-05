import java.util.Arrays;

public class DataQueryResult {

    private Pair<String, String> cipherData;
    private String[] cipherNonces;
    private int nrParticipants;
    private int pointer = 0;
    private boolean incFlag = false;

    public Pair<String, String> getCipherData() {
        return cipherData;
    }

    public DataQueryResult setCipherData(Pair<String, String> cipherData) {
        this.cipherData = cipherData;
        return this;
    }

    public String[] getCipherNonces() {
        return cipherNonces;
    }

    private DataQueryResult setCipherNonces(String[] cipherNonces) {
        this.cipherNonces = cipherNonces;
        this.pointer = 0;
        for(String nonce : cipherNonces) {
            if(nonce == null) break;
            pointer++;
        }
        return this;
    }

    /**
     * Adds a new nonce to the nonces array.
     * @param newCipherNonce the new nonce to be added.
     * @return returns this.
     */
    public DataQueryResult addToCipherNonces(String newCipherNonce) {
        if(isCipherNoncesFull()) throw new RuntimeException("The nonces array is already full");
        cipherNonces[pointer++] = newCipherNonce;
        return this;
    }

    public boolean isCipherNoncesFull() {
        return this.pointer == cipherNonces.length;
    }

    public int getNrParticipants() {
        return nrParticipants;
    }

    public DataQueryResult setNrParticipants(int nrParticipants) {
        this.nrParticipants = nrParticipants;
        return this;
    }

    public boolean isIncFlag() {
        return incFlag;
    }

    public DataQueryResult setIncFlag() {
        this.incFlag = true;
        return this;
    }

    /**
     * Factory method for DataQueryResult.
     * @param cipherData the ciphertext and exponent of the data encrypted with paillier encryption scheme.
     * @param nonces the nonces applied to the data in the process.
     * @param nrParticipants the number of participants that participated in the process.
     * @return a new DataQueryResult object.
     */
    public static DataQueryResult createInstance(Pair<String, String> cipherData, String[] nonces, int nrParticipants) {
        return new DataQueryResult()
                .setCipherData(cipherData)
                .setCipherNonces(nonces)
                .setNrParticipants(nrParticipants);
    }

    @Override
    public String toString() {
        return "    ciphertext data: " + this.cipherData.toString() +
                ",\n    ciphertext nonces: " + Arrays.toString(this.cipherNonces) +
                ",\n    number of participants: " + this.nrParticipants +
                ",\n    inconsistency flag: " + this.incFlag;
    }
}
