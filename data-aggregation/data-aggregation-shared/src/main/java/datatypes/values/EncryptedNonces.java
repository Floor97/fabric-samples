package datatypes.values;

import applications.operator.OperatorKeyStore;
import datatypes.aggregationprocess.AggregationProcess;
import encryption.NTRUEncryption;
import org.bouncycastler.crypto.InvalidCipherTextException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class EncryptedNonces {

    private final EncryptedNonce[] nonces;
    private int pointer;

    public EncryptedNonces(EncryptedNonce[] nonces) {
        this.nonces = nonces;
        setPointer();
    }

    /**
     * Makes the list of nonces encrypted with kp and adds them together, and re-encrypts the sum
     * with the postQuantumPk.
     *
     * @param keystore      keystore that contains the NTRU keys.
     * @param encNonces     the encrypted list of nonces.
     * @param postQuantumPk the public key to re-encrypt the nonces.
     * @return One nonce that is encrypted using the postQuantumPk.
     * @throws InvalidCipherTextException thrown by the NTRUEncrypt method.
     */
    public static EncryptedNonce condenseNonces(OperatorKeyStore keystore, EncryptedNonces encNonces, String postQuantumPk) throws InvalidCipherTextException {
        BigInteger summedNonce = new BigInteger("0");
        for (EncryptedNonce encNonce : encNonces.getNonces())
            summedNonce = summedNonce.add(new BigInteger(new String(keystore.getNtruEncryption().decrypt(encNonce.getNonce()))));
        return new EncryptedNonce(NTRUEncryption.encrypt(summedNonce.toString().getBytes(StandardCharsets.UTF_8), postQuantumPk));
    }

    /**
     * Takes the nonces and extracts the nonces at place index in each two-dimensional array in the
     * ArrayList. Returns those as a list.
     *
     * @param aggregationProcess the aggregation process the nonces correspond to.
     * @param index              the index.
     * @return a list of nonces.
     */
    public static EncryptedNonces getOperatorNonces(AggregationProcess aggregationProcess, int index) {
        ArrayList<EncryptedNonces> allNonces = aggregationProcess.getIpfsFile().getNonces();
        EncryptedNonces operatorNonces = new EncryptedNonces(new EncryptedNonce[allNonces.size()]);

        for (EncryptedNonces partNonces : allNonces)
            operatorNonces.addNonce(partNonces.getNonces()[index]);

        return operatorNonces;
    }

    /**
     * Sets the pointer to the first open spot in the nonces array.
     */
    private void setPointer() {
        this.pointer = 0;
        for (EncryptedNonce nonce : nonces) {
            if (nonce == null) return;
            this.pointer++;
        }
    }

    /**
     * Serializes the EncryptedNonces object.
     *
     * @param encryptedNonces the EncryptedNonces object.
     * @return the serialized EncryptedNonces object.
     */
    public static String serialize(EncryptedNonces encryptedNonces) {
        String[] strEncryptedNonces = new String[encryptedNonces.nonces.length];
        for (int i = 0; i < encryptedNonces.nonces.length; i++) {
            strEncryptedNonces[i] = encryptedNonces.nonces[i].serialize();
        }
        JSONObject json = new JSONObject();
        json.put("nonces", strEncryptedNonces); //todo check redundant
        return json.toString();
    }

    /**
     * Deserializes the EncryptedNonces object.
     *
     * @param serEncryptedNonces the serialized EncryptedNonces object.
     * @return the EncryptedNonces object.
     */
    public static EncryptedNonces deserialize(byte[] serEncryptedNonces) {
        return EncryptedNonces.deserialize(new String(serEncryptedNonces));
    }

    /**
     * Deserializes the EncryptedNonces object.
     *
     * @param serEncryptedNonces the serialized EncryptedNonces object.
     * @return the EncryptedNonces object.
     */
    public static EncryptedNonces deserialize(String serEncryptedNonces) {
        JSONObject json = new JSONObject(serEncryptedNonces);

        String[] encNonces = json.getJSONArray("nonces").toList().toArray(new String[0]);
        EncryptedNonce[] nonces = new EncryptedNonce[encNonces.length];
        for (int i = 0; i < nonces.length; i++) {
            nonces[i] = EncryptedNonce.deserialize(encNonces[i]);
        }
        return new EncryptedNonces(nonces);
    }

    public EncryptedNonce[] getNonces() {
        return nonces;
    }

    public void addNonce(EncryptedNonce nonce) {
        if (isFull()) throw new RuntimeException("Nonces are full");
        nonces[pointer++] = nonce;
    }

    public boolean isFull() {
        return this.pointer == nonces.length;
    }
}
