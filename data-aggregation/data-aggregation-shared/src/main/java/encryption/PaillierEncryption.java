package encryption;

import com.n1analytics.paillier.EncryptedNumber;
import com.n1analytics.paillier.PaillierContext;
import com.n1analytics.paillier.PaillierPrivateKey;
import com.n1analytics.paillier.PaillierPublicKey;
import datatypes.values.EncryptedData;

import java.math.BigInteger;

public class PaillierEncryption {

    private PaillierPrivateKey keys;

    public PaillierEncryption(int keySize) {
        this.keys = PaillierPrivateKey.create(keySize);
    }

    /**
     * Encrypts the data using the Paillier public key.
     *
     * @param data    the data that will be encrypted.
     * @param modulus the modulus of the Paillier public key.
     * @return the encrypted data.
     */
    public static EncryptedData encrypt(BigInteger data, String modulus) {
        PaillierPublicKey reformedPublicKey = new PaillierPublicKey(new BigInteger(modulus));
        PaillierContext ctx = reformedPublicKey.createUnsignedContext();
        EncryptedNumber encData = ctx.encrypt(data);
        return new EncryptedData(encData.calculateCiphertext().toString(), String.valueOf(encData.getExponent()));
    }

    /**
     * Decrypts the data using the Paillier private key.
     *
     * @param data the data that will be decrypted.
     * @return the decrypted data.
     */
    public BigInteger decrypt(EncryptedData data) {
        PaillierPublicKey pubkey = this.keys.getPublicKey();
        PaillierContext ctx = pubkey.createUnsignedContext();

        EncryptedNumber encryptedNumber = new EncryptedNumber(ctx, new BigInteger(data.getData()), Integer.parseInt(data.getExponent()));
        return this.keys.decrypt(encryptedNumber).decodeBigInteger();
    }

    /**
     * Serializes the PaillierPublicKey to String.
     *
     * @return the serialized PaillierPublicKey.
     */
    public String serialize() {
        return this.keys.getPublicKey().getModulus().toString();
    }

    /**
     * Deserializes the PaillierPublicKey to String.
     *
     * @param str the deserialized PaillierPublicKey.
     * @return the PaillierPublicKey.
     */
    public static PaillierPublicKey deserialize(String str) {
        return new PaillierPublicKey(new BigInteger(str));
    }

    public PaillierPublicKey getPublic() {
        return this.keys.getPublicKey();
    }
}
