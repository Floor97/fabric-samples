package datatypes.values;

import applications.operator.generators.NTRUEncryption;
import org.bouncycastler.crypto.AsymmetricCipherKeyPair;
import org.bouncycastler.crypto.InvalidCipherTextException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.Base64;

public class EncryptedNonces {

    private final EncryptedNonce[] nonces;

    public EncryptedNonces(EncryptedNonce[] nonces) {
        this.nonces = nonces;
    }

    public static String serialise(EncryptedNonces encryptedNonces) {
        String[] strEncryptedNonces = new String[encryptedNonces.nonces.length];
        for (int i = 0; i < encryptedNonces.nonces.length; i++) {
            strEncryptedNonces[i] = EncryptedNonce.serialise(encryptedNonces.nonces[i]);
        }
        JSONObject json = new JSONObject();
        json.put("nonces", strEncryptedNonces);
        return json.toString();
    }

    public static EncryptedNonces deserialise(String serEncryptedNonces) {
        JSONObject json = new JSONObject(serEncryptedNonces);

        String[] encNonces = json.getJSONArray("nonces").toList().toArray(new String[0]);
        EncryptedNonce[] nonces = new EncryptedNonce[encNonces.length];
        for(int i = 0; i < nonces.length; i++) {
            nonces[i] = new EncryptedNonce(Base64.getDecoder().decode(encNonces[i]));
        }
        return new EncryptedNonces(nonces);
    }

    public static EncryptedNonce condenseNonces(AsymmetricCipherKeyPair kp, String[] encNonces, String postQuantumPk) throws InvalidCipherTextException {
        BigInteger summedNonce = new BigInteger("0");
        for(String encNonce: encNonces) {
            byte[] encNonce2 = encNonce.getBytes();
            summedNonce = summedNonce.add(new BigInteger(NTRUEncryption.decrypt(encNonce2, kp)));
        }

        return new EncryptedNonce(NTRUEncryption.encrypt(summedNonce.toByteArray(), postQuantumPk));
    }

    public EncryptedNonce[] getNonces() {
        return nonces;
    }
}
