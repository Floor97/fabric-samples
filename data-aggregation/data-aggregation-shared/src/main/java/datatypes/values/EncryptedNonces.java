package datatypes.values;

import applications.operator.generators.NTRUEncryption;
import datatypes.aggregationprocess.AggregationProcess;
import org.bouncycastler.crypto.AsymmetricCipherKeyPair;
import org.bouncycastler.crypto.InvalidCipherTextException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.Arrays;

public class EncryptedNonces {

    private final EncryptedNonce[] nonces;
    private int pointer;

    public EncryptedNonces(EncryptedNonce... nonces) {
        this.nonces = nonces;
        setPointer();
    }

    public EncryptedNonces(byte[][] nonces) {
        this.nonces = new EncryptedNonce[nonces.length];
        for (int i = 0; i < nonces.length; i++) {
            this.nonces[i] = new EncryptedNonce(nonces[i]);
        }
        setPointer();
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

    private void setPointer() {
        this.pointer = 0;
        for (EncryptedNonce nonce : nonces) {
            if (nonce == null) break;
            this.pointer++;
        }
    }

    public static String serialize(EncryptedNonces encryptedNonces) {
        String[] strEncryptedNonces = new String[encryptedNonces.nonces.length];
        for (int i = 0; i < encryptedNonces.nonces.length; i++) {
            strEncryptedNonces[i] = EncryptedNonce.serialize(encryptedNonces.nonces[i]);
        }
        JSONObject json = new JSONObject();
        json.put("nonces", strEncryptedNonces); //todo check redundant
        return json.toString();
    }

    public static EncryptedNonces deserialize(byte[] serEncryptedNonces) {
        return EncryptedNonces.deserialize(new String(serEncryptedNonces));
    }

    public static EncryptedNonces deserialize(String serEncryptedNonces) {
        JSONObject json = new JSONObject(serEncryptedNonces);

        String[] encNonces = json.getJSONArray("nonces").toList().toArray(new String[0]);
        EncryptedNonce[] nonces = new EncryptedNonce[encNonces.length];
        for (int i = 0; i < nonces.length; i++) {
            nonces[i] = EncryptedNonce.deserialize(encNonces[i]);
        }
        return new EncryptedNonces(nonces);
    }

    public static EncryptedNonce condenseNonces(AsymmetricCipherKeyPair kp, EncryptedNonces encNonces, String postQuantumPk) throws InvalidCipherTextException {
        BigInteger summedNonce = new BigInteger("0");
        for (EncryptedNonce encNonce : encNonces.getNonces())
            summedNonce = summedNonce.add(new BigInteger(NTRUEncryption.decrypt(encNonce.getNonce(), kp)));

        return new EncryptedNonce(NTRUEncryption.encrypt(summedNonce.toByteArray(), postQuantumPk));
    }

    public static EncryptedNonces getOperatorNonces(AggregationProcess aggregationProcess, int index) {
        EncryptedNonces[] allNonces = aggregationProcess.getIpfsFile().getNonces();
        EncryptedNonces operatorNonces = new EncryptedNonces(new EncryptedNonce[allNonces.length]);

        for (EncryptedNonces partNonces : allNonces)
            operatorNonces.addNonce(partNonces.getNonces()[index]);

        return operatorNonces;
    }
}
