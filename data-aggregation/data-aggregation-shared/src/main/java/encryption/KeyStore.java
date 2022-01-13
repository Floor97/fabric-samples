package encryption;

import com.n1analytics.paillier.PaillierPublicKey;
import org.bouncycastler.pqc.crypto.ntru.NTRUEncryptionParameters;
import org.bouncycastler.pqc.crypto.ntru.NTRUEncryptionPublicKeyParameters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public interface KeyStore {

    /**
     * Serializes the PaillierPublicKey to String.
     *
     * @param pk the PaillierPublicKey.
     * @return the serialized PaillierPublicKey.
     */
    static String paPubKeyToString(PaillierPublicKey pk) {
        return pk.getModulus().toString();
    }

    /**
     * Deserializes the PaillierPublicKey to String.
     *
     * @param str the deserialized PaillierPublicKey.
     * @return the PaillierPublicKey.
     */
    static PaillierPublicKey paStringToPubKey(String str) {
        return new PaillierPublicKey(new BigInteger(str));
    }

    /**
     * Serializes the NTRUEncrypt public key to String.
     *
     * @param pk the NTRUEncrypt public key.
     * @return the serialized NTRUEncrypt public key.
     */
    static String pqPubKeyToString(NTRUEncryptionPublicKeyParameters pk) {
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
     * Converts the NTRUEncrypt public key to a byte array.
     *
     * @param pk the NTRUEncrypt public key.
     * @return the serialized NTRUEncrypt public.
     */
    static byte[] pqPubKeyToBytes(NTRUEncryptionPublicKeyParameters pk) {
        return (pqPubKeyToString(pk)).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Deserializes the NTRUEncrypt public key.
     *
     * @param str the deserialized NTRUEncrypt public key.
     * @return the NTRUEncrypt public key.
     */
    static NTRUEncryptionPublicKeyParameters pqToPubKey(byte[] str) {
        return KeyStore.pqToPubKey(new String(str));
    }

    /**
     * Deserializes the NTRUEncrypt public key.
     *
     * @param str the deserialized NTRUEncrypt public key.
     * @return the NTRUEncrypt public key.
     */
    static NTRUEncryptionPublicKeyParameters pqToPubKey(String str) {
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
}
