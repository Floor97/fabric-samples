package datatypes.values;

import applications.operator.generators.NTRUEncryption;
import datatypes.aggregationprocess.AggregationProcess;
import org.bouncycastler.crypto.AsymmetricCipherKeyPair;
import org.bouncycastler.crypto.InvalidCipherTextException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Base64;

public class EncryptedNonces {

    private final EncryptedNonce[] nonces;
    private int pointer;

    public EncryptedNonces(EncryptedNonce[] nonces) {
        this.nonces = nonces;
        setPointer();
    }

    public void addNonce(EncryptedNonce nonce) {
        if(isFull()) throw new RuntimeException("Nonces are full");
        nonces[pointer++] = nonce;
    }

    private void setPointer() {
        this.pointer = 0;
        for(EncryptedNonce nonce : nonces) {
            if(nonce == null) break;
            this.pointer++;
        }
    }

    public boolean isFull() {
        return this.pointer == nonces.length;
    }

    public static String serialize(EncryptedNonces encryptedNonces) {
        String[] strEncryptedNonces = new String[encryptedNonces.nonces.length];
        for (int i = 0; i < encryptedNonces.nonces.length; i++) {
            strEncryptedNonces[i] = EncryptedNonce.serialise(encryptedNonces.nonces[i]);
        }
        JSONObject json = new JSONObject();
        json.put("nonces", strEncryptedNonces);
        return json.toString();
    }

    public static EncryptedNonces deserialize(String serEncryptedNonces) {
        JSONObject json = new JSONObject(serEncryptedNonces);

        String[] encNonces = json.getJSONArray("nonces").toList().toArray(new String[0]);
        EncryptedNonce[] nonces = new EncryptedNonce[encNonces.length];
        for(int i = 0; i < nonces.length; i++) {
            nonces[i] = new EncryptedNonce(Base64.getDecoder().decode(encNonces[i]));
        }
        return new EncryptedNonces(nonces);
    }

    public static EncryptedNonce condenseNonces(AsymmetricCipherKeyPair kp, EncryptedNonces encNonces, String postQuantumPk) throws InvalidCipherTextException {
        BigInteger summedNonce = new BigInteger("0");
        for(EncryptedNonce encNonce: encNonces.getNonces()) {
            byte[] encNonce2 = encNonce.getNonce();
            summedNonce = summedNonce.add(new BigInteger(NTRUEncryption.decrypt(encNonce2, kp)));
        }

        return new EncryptedNonce(NTRUEncryption.encrypt(summedNonce.toByteArray(), postQuantumPk));
    }

    public EncryptedNonce[] getNonces() {
        return nonces;
    }

    public static EncryptedNonces getOperatorNonces(AggregationProcess aggregationProcess, int index) {
        EncryptedNonces operatorNonces = new EncryptedNonces(new EncryptedNonce[aggregationProcess.getData().getNrParticipants()]);

        for(EncryptedNonces nonces: aggregationProcess.getData().getCipherNonces())
            operatorNonces.addNonce(nonces.getNonces()[index]);

        return operatorNonces;
    }
    @Override
    public String toString() {
        return Arrays.toString(nonces);
    }
}
