package applications.operator.generators;

import datatypes.values.EncryptedData;
import datatypes.values.EncryptedNonce;
import datatypes.values.EncryptedNonces;
import org.bouncycastler.crypto.InvalidCipherTextException;
import shared.Pair;

import java.math.BigInteger;
import java.util.Random;

public class DataGenerator {

    /**
     * Selects a random int and encrypts it with the given modulus.
     * @param modulus the modulus of the public key of the paillier public key.
     * @return encrypted data.
     */
    public static Pair<EncryptedData, EncryptedNonces> generateDataAndNonces(String modulus, String[] postQuantumPks) throws InvalidCipherTextException {
        BigInteger data = new BigInteger(String.valueOf(new Random().nextInt(Integer.MAX_VALUE)));
        EncryptedNonces nonces = new EncryptedNonces(new EncryptedNonce[postQuantumPks.length]);
        for (String postQuantumPk : postQuantumPks) {
            BigInteger nonce = new BigInteger(String.valueOf(new Random().nextInt(Integer.MAX_VALUE)));
            data = data.add(nonce);

            nonces.addNonce(new EncryptedNonce(NTRUEncryption.encrypt(String.valueOf(nonce).getBytes(), postQuantumPk)));
        }
        return new Pair<>(PaillierEncryption.encrypt(data, modulus), nonces);
    }
}
