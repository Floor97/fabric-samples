package applications;

import com.n1analytics.paillier.PaillierPrivateKey;
import com.n1analytics.paillier.PaillierPublicKey;
import org.bouncycastler.crypto.AsymmetricCipherKeyPair;
import org.bouncycastler.pqc.crypto.ntru.NTRUEncryptionKeyGenerationParameters;
import org.bouncycastler.pqc.crypto.ntru.NTRUEncryptionKeyPairGenerator;
import org.bouncycastler.pqc.crypto.ntru.NTRUEncryptionPublicKeyParameters;
import org.bouncycastler.pqc.crypto.ntru.NTRUParameters;
import shared.Pair;

public class DataQueryKeyStore implements KeyStore {

    private PaillierPrivateKey paillierKeys;
    private AsymmetricCipherKeyPair postQuantumKeys;

    public PaillierPrivateKey getPaillierKeys() {
        return this.paillierKeys;
    }

    public DataQueryKeyStore setPaillierKeys() {
        paillierKeys = PaillierPrivateKey.create(2048);
        return this;
    }

    public AsymmetricCipherKeyPair getPostQuantumKeys() {
        return this.postQuantumKeys;
    }

    public DataQueryKeyStore setPostQuantumKeys() {
        NTRUEncryptionKeyGenerationParameters params = NTRUEncryptionKeyGenerationParameters.APR2011_743_FAST.clone();
        params.polyType = NTRUParameters.TERNARY_POLYNOMIAL_TYPE_SIMPLE;
        NTRUEncryptionKeyPairGenerator ntruGen = new NTRUEncryptionKeyPairGenerator();
        ntruGen.init(params);
        this.postQuantumKeys = ntruGen.generateKeyPair();
        return this;
    }

    public Pair<PaillierPublicKey, NTRUEncryptionPublicKeyParameters> getPublicKeys() {
        return new Pair(paillierKeys.getPublicKey(), postQuantumKeys.getPublic());
    }

    public static DataQueryKeyStore createInstance() {
        return new DataQueryKeyStore().setPaillierKeys().setPostQuantumKeys();
    }




}
