package applications.operator;

import encryption.KeyStore;
import org.bouncycastler.pqc.crypto.ntru.NTRUEncryptionKeyGenerationParameters;

public class OperatorKeyStore extends KeyStore {

    private int index;

    public OperatorKeyStore(NTRUEncryptionKeyGenerationParameters params) {
        super(params);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

}
