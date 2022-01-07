package datatypes.values;

import java.math.BigInteger;

public class EncryptedData {

    private final BigInteger data;
    private final int exponent;

    public EncryptedData(BigInteger data, int exponent) {
        this.data = data;
        this.exponent = exponent;
    }

    public static String serialise(EncryptedData data) {
        return data.data + ":" + data.exponent;
    }

    public static EncryptedData deserialise(String data) {
        String[] parts = data.split(":", 2);
        return new EncryptedData(new BigInteger(parts[0]), Integer.parseInt(parts[1]));
    }

    public BigInteger getData() {
        return data;
    }

    public int getExponent() {
        return exponent;
    }
}
