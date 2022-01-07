package datatypes.values;

public class EncryptedData {

    private final String data;
    private final String exponent;

    public EncryptedData(String data, String exponent) {
        this.data = data;
        this.exponent = exponent;
    }

    public static String serialize(EncryptedData data) {
        return data.data + ":" + data.exponent;
    }

    public static EncryptedData deserialise(String data) {
        String[] parts = data.split(":", 2);
        return new EncryptedData(parts[0], parts[1]);
    }

    public String getData() {
        return data;
    }

    public String getExponent() {
        return exponent;
    }

    @Override
    public String toString() {
        return "data: " + data + ", exponent: " + exponent;
    }
}
