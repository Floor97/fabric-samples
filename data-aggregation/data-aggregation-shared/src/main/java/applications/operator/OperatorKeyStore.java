package applications.operator;

import encryption.KeyStore;
import org.bouncycastler.crypto.AsymmetricCipherKeyPair;
import org.bouncycastler.pqc.crypto.ntru.NTRUEncryptionKeyGenerationParameters;
import org.bouncycastler.pqc.crypto.ntru.NTRUEncryptionKeyPairGenerator;
import org.bouncycastler.pqc.crypto.ntru.NTRUEncryptionPublicKeyParameters;
import org.bouncycastler.pqc.crypto.ntru.NTRUParameters;

public class OperatorKeyStore implements KeyStore {

    private AsymmetricCipherKeyPair postQuantumKeys;
    private int index;

    /**
     * Generates a new set of NTRUEncrypt keys and sets them in postQuantumKeys.
     *
     * @return the OperatorKeyStore instance.
     */
    private OperatorKeyStore setPostQuantumKeys() {
        NTRUEncryptionKeyGenerationParameters params = NTRUEncryptionKeyGenerationParameters.APR2011_743_FAST.clone();
        params.polyType = NTRUParameters.TERNARY_POLYNOMIAL_TYPE_SIMPLE;
        NTRUEncryptionKeyPairGenerator ntruGen = new NTRUEncryptionKeyPairGenerator();
        ntruGen.init(params);
        this.postQuantumKeys = ntruGen.generateKeyPair();
        return this;
    }

    public AsymmetricCipherKeyPair getPostQuantumKeys() {
        return this.postQuantumKeys;
    }

    public NTRUEncryptionPublicKeyParameters getPublicKey() {
        return (NTRUEncryptionPublicKeyParameters) postQuantumKeys.getPublic();
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public static OperatorKeyStore createInstance() {
        return new OperatorKeyStore().setPostQuantumKeys();
    }
}
