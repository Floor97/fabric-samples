package applications.operator.generators;

import org.bouncycastler.crypto.AsymmetricCipherKeyPair;
import org.bouncycastler.crypto.InvalidCipherTextException;
import shared.Pair;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class DataGenerator {

    /**
     * Selects a random int and encrypts it with the given modulus.
     * @param modulus the modulus of the public key of the paillier public key.
     * @return encrypted data.
     */
    public static Pair<Pair<String, Integer>, byte[][]> generateDataAndNonces(String modulus, String[] postQuantumPks) throws InvalidCipherTextException {
        BigInteger data = new BigInteger(String.valueOf(new Random().nextInt())); //The actual data
        byte[][] nonces = new byte[postQuantumPks.length][];
        for(int i = 0; i < postQuantumPks.length; i++) {
            BigInteger nonce = new BigInteger(String.valueOf(new Random().nextInt()));
            data = data.add(nonce);

            nonces[i] = NTRUEncryption.encrypt(String.valueOf(nonce).getBytes(), postQuantumPks[i]);
        }
        return new Pair<>(PaillierEncryption.encrypt(data, modulus), nonces);
    }
}
