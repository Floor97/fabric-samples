package applications.asker;

import com.n1analytics.paillier.PaillierPublicKey;
import encryption.KeyStore;
import encryption.PaillierEncryption;
import org.bouncycastler.pqc.crypto.ntru.NTRUEncryptionKeyGenerationParameters;

public class DataQueryKeyStore extends KeyStore {

    private final PaillierEncryption paillierEncryption;

    public DataQueryKeyStore(NTRUEncryptionKeyGenerationParameters params, int keysize) {
        super(params);
        this.paillierEncryption = new PaillierEncryption(keysize);
    }

    /**
     * Gets and returns the public keys used in the Paillier encryption.
     *
     * @return the public key used in Paillier encryption.
     */
    public PaillierPublicKey getPaillierPublic() {
        return paillierEncryption.getPublic();
    }

    public PaillierEncryption getPaillierEncryption() {
        return paillierEncryption;
    }
}
