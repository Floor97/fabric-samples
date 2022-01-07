package datatypes.dataquery;

import datatypes.values.EncryptedData;
import datatypes.values.EncryptedNonce;
import datatypes.values.EncryptedNonces;

public class DataQueryResult {

    private EncryptedData cipherData;
    private EncryptedNonces cipherNonces;
    private int nrParticipants;
    private boolean incFlag = false;

    public EncryptedData getCipherData() {
        return cipherData;
    }

    public DataQueryResult setCipherData(EncryptedData cipherData) {
        this.cipherData = cipherData;
        return this;
    }

    public EncryptedNonces getCipherNonces() {
        return cipherNonces;
    }

    private DataQueryResult setCipherNonces(EncryptedNonces cipherNonces) {
        this.cipherNonces = cipherNonces;
        return this;
    }

    /**
     * Adds a new nonce to the nonces array.
     * @param newCipherNonce the new nonce to be added.
     * @return returns this.
     */
    public DataQueryResult addToCipherNonces(EncryptedNonce newCipherNonce) {
        this.cipherNonces.addNonce(newCipherNonce);
        return this;
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
    public static DataQueryResult createInstance(EncryptedData cipherData, EncryptedNonces nonces, int nrParticipants) {
        return new DataQueryResult()
                .setCipherData(cipherData)
                .setCipherNonces(nonces)
                .setNrParticipants(nrParticipants);
    }

    @Override
    public String toString() {
        return "    ciphertext data: " + this.cipherData +
                ",\n    ciphertext nonces: " + this.cipherNonces +
                ",\n    number of participants: " + this.nrParticipants +
                ",\n    inconsistency flag: " + this.incFlag;
    }
}
