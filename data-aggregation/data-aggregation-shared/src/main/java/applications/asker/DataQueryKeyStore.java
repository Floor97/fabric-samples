package applications.asker;

import com.n1analytics.paillier.PaillierPrivateKey;
import com.n1analytics.paillier.PaillierPublicKey;
import datatypes.values.Pair;
import encryption.KeyStore;
import org.bouncycastler.crypto.AsymmetricCipherKeyPair;
import org.bouncycastler.pqc.crypto.ntru.NTRUEncryptionKeyGenerationParameters;
import org.bouncycastler.pqc.crypto.ntru.NTRUEncryptionKeyPairGenerator;
import org.bouncycastler.pqc.crypto.ntru.NTRUEncryptionPublicKeyParameters;
import org.bouncycastler.pqc.crypto.ntru.NTRUParameters;

public class DataQueryKeyStore implements KeyStore {

    private PaillierPrivateKey paillierKeys;
    private AsymmetricCipherKeyPair postQuantumKeys;

    public DataQueryKeyStore() {
        setPaillierKeys();
        setPostQuantumKeys();
    }

    /**
     * Generates a new set of Paillier keys and sets them in paillierKeys.
     */
    private void setPaillierKeys() {
        this.paillierKeys = PaillierPrivateKey.create(2048);
    }


    /**
     * Generates a new set of NTRUEncrypt keys and sets them in postQuantumKeys.
     */
    private void setPostQuantumKeys() {
        NTRUEncryptionKeyGenerationParameters params = NTRUEncryptionKeyGenerationParameters.APR2011_743_FAST.clone();
        params.polyType = NTRUParameters.TERNARY_POLYNOMIAL_TYPE_SIMPLE;
        NTRUEncryptionKeyPairGenerator ntruGen = new NTRUEncryptionKeyPairGenerator();
        ntruGen.init(params);
        this.postQuantumKeys = ntruGen.generateKeyPair();
    }

    /**
     * Returns the public keys of both the Paillier and NTRUEncrypt schemes.
     *
     * @return public keys of Paillier and NTRUEncrypt.
     */
    public Pair<PaillierPublicKey, NTRUEncryptionPublicKeyParameters> getPublicKeys() {
        return new Pair<>(paillierKeys.getPublicKey(), (NTRUEncryptionPublicKeyParameters) postQuantumKeys.getPublic());
    }

    public PaillierPrivateKey getPaillierKeys() {
        return this.paillierKeys;
    }

    public AsymmetricCipherKeyPair getPostQuantumKeys() {
        return this.postQuantumKeys;
    }
}
