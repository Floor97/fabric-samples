package encryption;

import com.n1analytics.paillier.EncryptedNumber;
import com.n1analytics.paillier.PaillierContext;
import com.n1analytics.paillier.PaillierPrivateKey;
import com.n1analytics.paillier.PaillierPublicKey;
import datatypes.values.EncryptedData;

import java.math.BigInteger;

public class PaillierEncryption {

    /**
     * Encrypts the data using the Paillier public key.
     *
     * @param data    the data that will be encrypted.
     * @param modulus the modulus of the Paillier public key.
     * @return the encrypted data.
     */
    public static EncryptedData encrypt(BigInteger data, String modulus) {
        PaillierPublicKey reformedPublicKey = new PaillierPublicKey(new BigInteger(modulus));
        PaillierContext ctx = reformedPublicKey.createUnsignedContext();
        EncryptedNumber encData = ctx.encrypt(data);
        return new EncryptedData(encData.calculateCiphertext().toString(), String.valueOf(encData.getExponent()));
    }

    /**
     * Decrypts the data using the Paillier private key.
     *
     * @param data       the data that will be decrypted.
     * @param privateKey the private key.
     * @return the decrypted data.
     */
    public static BigInteger decrypt(EncryptedData data, PaillierPrivateKey privateKey) {
        PaillierPublicKey pubkey = privateKey.getPublicKey();
        PaillierContext ctx = pubkey.createUnsignedContext();

        EncryptedNumber encryptedNumber = new EncryptedNumber(ctx, new BigInteger(data.getData()), Integer.parseInt(data.getExponent()));
        return privateKey.decrypt(encryptedNumber).decodeBigInteger();
    }
}
