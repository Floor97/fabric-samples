package encryption;

import org.bouncycastler.crypto.AsymmetricCipherKeyPair;
import org.bouncycastler.crypto.InvalidCipherTextException;
import org.bouncycastler.pqc.crypto.ntru.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

public class NTRUEncryption {

    private static final NTRUEngine ntru = new NTRUEngine();
    private AsymmetricCipherKeyPair keys;

    public NTRUEncryption(NTRUEncryptionKeyGenerationParameters params) {
        params.polyType = NTRUParameters.TERNARY_POLYNOMIAL_TYPE_SIMPLE;
        NTRUEncryptionKeyPairGenerator ntruGen = new NTRUEncryptionKeyPairGenerator();
        ntruGen.init(params);
        this.keys = ntruGen.generateKeyPair();
    }

    /**
     * Encrypts the data with the NTRUEncrypt public key.
     *
     * @param data    the data that will be encrypted.
     * @param serPqpk the public key.
     * @return the encrypted data.
     * @throws InvalidCipherTextException thrown by processBlock method.
     */
    public static byte[] encrypt(byte[] data, String serPqpk) throws InvalidCipherTextException {
        NTRUEncryptionPublicKeyParameters pqpk = NTRUEncryption.deserialize(serPqpk);
        ntru.init(true, pqpk);
        return ntru.processBlock(data, 0, data.length);
    }

    /**
     * Decrypts the data with the NTRUEncrypt private key.
     *
     * @param encData the encrypted data.
     * @return the decrypted data.
     * @throws InvalidCipherTextException thrown by processBlock method.
     */
    public byte[] decrypt(byte[] encData) throws InvalidCipherTextException {
        ntru.init(false, this.keys.getPrivate());
        return ntru.processBlock(encData, 0, encData.length);
    }

    /**
     * Serializes the NTRUEncrypt public key to String.
     *
     * @return the serialized NTRUEncrypt public key.
     */
    public static String serialize(NTRUEncryptionPublicKeyParameters pk) {
        if (pk == null) return "null";
        ByteArrayOutputStream pubOut = new ByteArrayOutputStream();
        ByteArrayOutputStream parOut = new ByteArrayOutputStream();
        try {
            pk.writeTo(pubOut);
            pk.getParameters().writeTo(parOut);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String pubKey = Base64.getEncoder().encodeToString(pubOut.toByteArray());
        String stParams = Base64.getEncoder().encodeToString(parOut.toByteArray());
        return pubKey + ":" + stParams;
    }

    /**
     * Deserializes the NTRUEncrypt public key.
     *
     * @param str the deserialized NTRUEncrypt public key.
     * @return the NTRUEncrypt public key.
     */
    public static NTRUEncryptionPublicKeyParameters deserialize(byte[] str) {
        return NTRUEncryption.deserialize(new String(str));
    }

    /**
     * Deserializes the NTRUEncrypt public key.
     *
     * @param str the deserialized NTRUEncrypt public key.
     * @return the NTRUEncrypt public key.
     */
    public static NTRUEncryptionPublicKeyParameters deserialize(String str) {
        if (str.equals("null")) return null;

        String[] parts = str.split(":", 2);
        byte[] pubKey = Base64.getDecoder().decode(parts[0]);
        byte[] stParams = Base64.getDecoder().decode(parts[1]);

        NTRUEncryptionParameters params = null;
        try {
            params = new NTRUEncryptionParameters(new ByteArrayInputStream(stParams));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new NTRUEncryptionPublicKeyParameters(pubKey, params);
    }

    public NTRUEncryptionPublicKeyParameters getPublic() {
        return (NTRUEncryptionPublicKeyParameters) keys.getPublic();
    }
}
