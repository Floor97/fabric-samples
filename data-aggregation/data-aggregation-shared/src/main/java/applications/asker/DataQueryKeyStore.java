package applications.asker;

import com.n1analytics.paillier.PaillierPrivateKey;
import com.n1analytics.paillier.PaillierPublicKey;
import encryption.KeyStore;
import org.bouncycastler.crypto.AsymmetricCipherKeyPair;
import org.bouncycastler.pqc.crypto.ntru.NTRUEncryptionKeyGenerationParameters;
import org.bouncycastler.pqc.crypto.ntru.NTRUEncryptionKeyPairGenerator;
import org.bouncycastler.pqc.crypto.ntru.NTRUEncryptionPublicKeyParameters;
import org.bouncycastler.pqc.crypto.ntru.NTRUParameters;
import datatypes.values.Pair;

public class DataQueryKeyStore implements KeyStore {

    private PaillierPrivateKey paillierKeys;
    private AsymmetricCipherKeyPair postQuantumKeys;

    public DataQueryKeyStore() {
        setPaillierKeys();
        setPostQuantumKeys();
    }

    public PaillierPrivateKey getPaillierKeys() {
        return this.paillierKeys;
    }

    private void setPaillierKeys() {
        this.paillierKeys = PaillierPrivateKey.create(2048);
    }

    public AsymmetricCipherKeyPair getPostQuantumKeys() {
        return this.postQuantumKeys;
    }

    private void setPostQuantumKeys() {
        NTRUEncryptionKeyGenerationParameters params = NTRUEncryptionKeyGenerationParameters.APR2011_743_FAST.clone();
        params.polyType = NTRUParameters.TERNARY_POLYNOMIAL_TYPE_SIMPLE;
        NTRUEncryptionKeyPairGenerator ntruGen = new NTRUEncryptionKeyPairGenerator();
        ntruGen.init(params);
        this.postQuantumKeys = ntruGen.generateKeyPair();
    }

    public Pair<PaillierPublicKey, NTRUEncryptionPublicKeyParameters> getPublicKeys() {
        return new Pair<>(paillierKeys.getPublicKey(), (NTRUEncryptionPublicKeyParameters) postQuantumKeys.getPublic());
    }
}
