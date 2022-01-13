package encryption;

import org.bouncycastler.crypto.AsymmetricCipherKeyPair;
import org.bouncycastler.crypto.InvalidCipherTextException;
import org.bouncycastler.pqc.crypto.ntru.NTRUEncryptionPublicKeyParameters;
import org.bouncycastler.pqc.crypto.ntru.NTRUEngine;

public class NTRUEncryption {

    private static final NTRUEngine ntru = new NTRUEngine();

    /**
     * Encrypts the data with the NTRUEncrypt public key.
     *
     * @param data    the data that will be encrypted.
     * @param serPqpk the public key.
     * @return the encrypted data.
     * @throws InvalidCipherTextException thrown by processBlock method.
     */
    public static byte[] encrypt(byte[] data, String serPqpk) throws InvalidCipherTextException {
        NTRUEncryptionPublicKeyParameters pqpk = KeyStore.pqToPubKey(serPqpk);
        ntru.init(true, pqpk);
        return ntru.processBlock(data, 0, data.length);
    }

    /**
     * Decrypts the data with the NTRUEncrypt private key.
     *
     * @param encData the encrypted data.
     * @param kp      the private key.
     * @return the decrypted data.
     * @throws InvalidCipherTextException thrown by processBlock method.
     */
    public static byte[] decrypt(byte[] encData, AsymmetricCipherKeyPair kp) throws InvalidCipherTextException {
        ntru.init(false, kp.getPrivate());
        return ntru.processBlock(encData, 0, encData.length);
    }
}
