package datatypes.values;

public class EncryptedData {

    private String data;
    private String exponent;

    public EncryptedData(String data, String exponent) {
        this.data = data;
        this.exponent = exponent;
    }

    /**
     * Serializes the EncryptedData object into a String.
     *
     * @return the serialized EncryptedData object.
     */
    public String serialize() {
        return this.data + ":" + this.exponent;
    }

    /**
     * The EncryptedData object is deserialized.
     *
     * @param encData the serialized EncryptedData object.
     * @return the deserialized EncryptedData object.
     */
    public static EncryptedData deserialize(byte[] encData) {
        return EncryptedData.deserialize(new String(encData));
    }

    /**
     * The EncryptedData object is deserialized.
     *
     * @param encData the serialized EncryptedData object.
     * @return the deserialized EncryptedData object.
     */
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
