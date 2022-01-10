package datatypes.values;

public class EncryptedData {

    private String data;
    private String exponent;

    public EncryptedData(String data, String exponent) {
        this.data = data;
        this.exponent = exponent;
    }

    public static String serialize(EncryptedData encData) {
        return encData.data + ":" + encData.exponent;
    }

    public static EncryptedData deserialize(byte[] encData) {
        return EncryptedData.deserialize(new String(encData));
    }

    public static EncryptedData deserialize(String encData) {
        String[] parts = encData.split(":", 2);
        return new EncryptedData(parts[0], parts[1]);
    }

    public String getData() {
        return data;
    }

    public EncryptedData setData(String data) {
        this.data = data;
        return this;
    }

    public String getExponent() {
        return exponent;
    }

    public EncryptedData setExponent(String exponent) {
        this.exponent = exponent;
        return this;
    }

    @Override
    public String toString() {
        return "data: " + data + ", exponent: " + exponent;
    }
}
