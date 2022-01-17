package encryption;

import org.bouncycastler.pqc.crypto.ntru.NTRUEncryptionKeyGenerationParameters;
import org.bouncycastler.pqc.crypto.ntru.NTRUEncryptionPublicKeyParameters;

public class KeyStore {

    private final NTRUEncryption ntruEncryption;

    public KeyStore(NTRUEncryptionKeyGenerationParameters params) {
        this.ntruEncryption = new NTRUEncryption(params);
    }

    /**
     * Returns the public key of the NTRUEncrypt scheme used in encryption.
     *
     * @return the public key.
     */
    public NTRUEncryptionPublicKeyParameters getNtruPublic() {
        return this.ntruEncryption.getPublic();
    }

    public NTRUEncryption getNtruEncryption() {
        return ntruEncryption;
    }

    /**
     * Deserializes the KeyStore by returning the serialized NTRUEncryption object.
     *
     * @return the serialized NTRUEncryption object.
     */
    public String serialize() {
        return NTRUEncryption.serialize(ntruEncryption.getPublic());
    }
}
