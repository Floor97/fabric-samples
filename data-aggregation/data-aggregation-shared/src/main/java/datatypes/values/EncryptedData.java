package datatypes.values;

public class EncryptedData {

    private String data;
    private String exponent;

    public EncryptedData(String data, String exponent) {
        this.data = data;
        this.exponent = exponent;
    }

    public static String serialize(EncryptedData data) {
        return data.data + ":" + data.exponent;
    }

    public static EncryptedData deserialize(String data) {
        String[] parts = data.split(":", 2);
        return new EncryptedData(parts[0], parts[1]);
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getExponent() {
        return exponent;
    }

    public void setExponent(String exponent) {
        this.exponent = exponent;
    }

    @Override
    public String toString() {
        return "data: " + data + ", exponent: " + exponent;
    }
}
