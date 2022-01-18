package applications.operator;

import datatypes.values.Pair;
import org.bouncycastler.crypto.InvalidCipherTextException;

import java.io.File;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.util.Random;

public class DataAndNonces {

    /**
     * Selects a random positive int for both the data and the nonces. Obfuscates
     * the data with the generated nonces.
     *
     * @param nrOperators the number of operators in the process.
     * @return obfuscated data and a list of nonces.
     */
    public static Pair<BigInteger, BigInteger[]> generateDataAndNonces(int nrOperators) throws InvalidCipherTextException {
        BigInteger data = new BigInteger(DataAndNonces.getData());
        System.out.println("data: " + data);
        BigInteger[] nonces = new BigInteger[nrOperators];
        for(int i = 0; i < nrOperators; i++) {
            BigInteger nonce = new BigInteger(String.valueOf(new Random().nextInt(Integer.MAX_VALUE)));
            data = data.add(nonce);
            System.out.println("nonce: " + nonce);

            nonces[i] = nonce;
        }
        return new Pair<>(data, nonces);
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
            int line = new Random().nextInt(7000);
            System.out.println("line: " + line);
            file.seek(28 * line + 16);
            return file.readLine().substring(20, 25);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Could not find dataset");
        }
    }
}
