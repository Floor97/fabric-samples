package applications.operator;

import datatypes.values.EncryptedData;
import datatypes.values.EncryptedNonce;
import datatypes.values.EncryptedNonces;
import datatypes.values.Pair;
import encryption.NTRUEncryption;
import encryption.PaillierEncryption;
import org.bouncycastler.crypto.InvalidCipherTextException;

import java.io.File;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.util.Random;

public class DataAndNonces {

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
        BigInteger data = new BigInteger(DataAndNonces.getData());
        System.out.println("data: " + data);
        EncryptedNonces nonces = new EncryptedNonces(new EncryptedNonce[postQuantumPks.length]);
        for (String postQuantumPk : postQuantumPks) {
            String nonce = String.valueOf(new Random().nextInt(Integer.MAX_VALUE));
            data = data.add(new BigInteger(nonce));
            System.out.println("nonce: " + nonce);

            nonces.addNonce(new EncryptedNonce(NTRUEncryption.encrypt(nonce.getBytes(), postQuantumPk)));
        }
        return new Pair<>(PaillierEncryption.encrypt(data, modulus), nonces);
    }

    /**
     * Reads a random data entry from a dataset.
     *
     * @return the data entry from the dataset.
     */
    private static String getData() {
        try {
            File f = new File("src/main/resources/AEP_hourly.csv");
            RandomAccessFile file = new RandomAccessFile(f, "r");
            int line = new Random().nextInt(10000);
            System.out.println("line: " + line);
            file.seek(28 * line + 16);
            return file.readLine().substring(20, 25);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Could not find dataset");
        }
    }
}
