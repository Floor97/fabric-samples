package datatypes.values;

import java.util.Base64;

public class EncryptedNonce {

    private final byte[] nonce;

    public EncryptedNonce(byte[] nonce) {
        this.nonce = nonce;
    }

    public static EncryptedNonce deserialise(String serNonce) {
        return new EncryptedNonce(Base64.getDecoder().decode(serNonce));
    }

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
