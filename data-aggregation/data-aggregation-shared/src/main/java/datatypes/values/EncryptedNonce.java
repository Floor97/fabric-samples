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
        if (serNonce.equals("null")) return null;
        return new EncryptedNonce(Base64.getDecoder().decode(serNonce));
    }

    /**
     * Serializes the EncryptedNonce object.
     *
     * @return the serialized EncryptedNonce object.
     */
    public String serialize() {
        return Base64.getEncoder().encodeToString(this.nonce);
    }

    public byte[] getNonce() {
        return nonce;
    }

    @Override
    public String toString() {
        return this.serialize();
    }
}
