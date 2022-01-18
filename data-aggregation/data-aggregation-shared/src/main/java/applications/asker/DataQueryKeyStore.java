package applications.asker;

import encryption.KeyStore;
import encryption.PaillierEncryption;
import org.bouncycastler.pqc.crypto.ntru.NTRUEncryptionKeyGenerationParameters;

public class DataQueryKeyStore extends KeyStore {

    private final PaillierEncryption paillierEncryption;

    public DataQueryKeyStore(NTRUEncryptionKeyGenerationParameters params, int keysize) {
        super(params);
        this.paillierEncryption = new PaillierEncryption(keysize);
    }

    public PaillierEncryption getPaillierEncryption() {
        return paillierEncryption;
    }
}
