package applications.operator.generators;

import applications.KeyStore;
import org.bouncycastler.crypto.AsymmetricCipherKeyPair;
import org.bouncycastler.crypto.InvalidCipherTextException;
import org.bouncycastler.pqc.crypto.ntru.NTRUEncryptionPublicKeyParameters;
import org.bouncycastler.pqc.crypto.ntru.NTRUEngine;

public class NTRUEncryption {

    private static final NTRUEngine ntru = new NTRUEngine();

    public static byte[] encrypt(byte[] data, String serPqpk) throws InvalidCipherTextException {
        NTRUEncryptionPublicKeyParameters pqpk =  KeyStore.pqToPubKey(serPqpk);
        ntru.init(true, pqpk);
        return ntru.processBlock(data, 0, data.length);
    }

    public static byte[] decrypt(byte[] encData, AsymmetricCipherKeyPair kp) throws InvalidCipherTextException {
        ntru.init(false, kp.getPrivate());
        return ntru.processBlock(encData, 0, encData.length);
    }
}
