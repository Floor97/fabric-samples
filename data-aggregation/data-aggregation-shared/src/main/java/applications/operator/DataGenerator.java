package applications.operator;

import com.n1analytics.paillier.PaillierPrivateKey;
import datatypes.values.EncryptedData;
import datatypes.values.EncryptedNonce;
import datatypes.values.EncryptedNonces;
import datatypes.values.Pair;
import encryption.NTRUEncryption;
import encryption.PaillierEncryption;
import org.bouncycastler.crypto.InvalidCipherTextException;

import java.math.BigInteger;
import java.util.Random;

public class DataGenerator {

    /**
     * Selects a random positive int for both the data and the nonces. Obfuscates
     * the data with the generated nonces. Encrypts the obfuscated data with the
     * Paillier encryption scheme and the nonces with the NTRUEncrypt scheme.
     *
     * @param modulus        the modulus of the public key of the paillier public key.
     * @param postQuantumPks the NTRUEncrypt public key.
     * @return encrypted obfuscated data and a list of encrypted nonces.
     * @throws InvalidCipherTextException thrown by the NTRUEncrypt encrypt method.
     */
    public static Pair<EncryptedData, EncryptedNonces> generateDataAndNonces(String modulus, String[] postQuantumPks) throws InvalidCipherTextException {
        BigInteger data = new BigInteger(String.valueOf(new Random().nextInt(Integer.MAX_VALUE)));
        System.out.println("data: " + data);
        EncryptedNonces nonces = new EncryptedNonces(new EncryptedNonce[postQuantumPks.length]);
        for (String postQuantumPk : postQuantumPks) {
            String nonce = String.valueOf(new Random().nextInt(Integer.MAX_VALUE));
            data = data.add(new BigInteger(nonce));

            nonces.addNonce(new EncryptedNonce(NTRUEncryption.encrypt(nonce.getBytes(), postQuantumPk)));
        }
        return new Pair<EncryptedData, EncryptedNonces>(PaillierEncryption.encrypt(data, modulus), nonces);
    }
}
