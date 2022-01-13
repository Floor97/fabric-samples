package datatypes.values;

import java.util.Base64;

public class EncryptedNonce {

    private final byte[] nonce;

    public EncryptedNonce(byte[] nonce) {
        this.nonce = nonce;
    }

    /**
     * Deserializes the EncryptedNonce object.
     *
     * @param serNonce the serialized EncryptedNonce object.
     * @return the EncryptedNonce object.
     */
    public static EncryptedNonce deserialize(String serNonce) {
        return new EncryptedNonce(Base64.getDecoder().decode(serNonce));
    }

    /**
     * Serializes the EncryptedNonce object.
     *
     * @param encryptedNonce the EncryptedNonce object.
     * @return the serialized EncryptedNonce object.
     */
    public static String serialize(EncryptedNonce encryptedNonce) {
        return Base64.getEncoder().encodeToString(encryptedNonce.nonce);
    }

    public byte[] getNonce() {
        return nonce;
    }

    @Override
    public String toString() {
        return serialize(this);
    }
}
