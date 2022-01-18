package applications.asker;


import datatypes.values.IPFSFile;

import java.math.BigInteger;
import java.util.Arrays;

public class DataQueryIPFSFile extends IPFSFile {

    private final BigInteger[] nonces;
    private int pointer = 0;

    public DataQueryIPFSFile(BigInteger data, BigInteger[] nonces) {
        super(data);
        this.nonces = nonces;
        setPointer();
    }

    public BigInteger[] getNonces() {
        return nonces;
    }

    public void addNonce(BigInteger nonce) {
        if (isFullNonces()) throw new RuntimeException("Nonces array is full!");
        this.nonces[this.pointer++] = nonce;
    }

    private void setPointer() {
        this.pointer = 0;
        for (BigInteger nonce : nonces) {
            if (nonce == null) break;
            pointer++;
        }
    }

    public boolean isFullNonces() {
        return this.pointer >= this.nonces.length;
    }

    /**
     * The DataQueryIPFSFile gets serialized into a String.
     *
     * @return the serialized DataQueryIPFSFile.
     */
    public String serialize() {
        String superStr = super.serialize();
        StringBuilder builder = new StringBuilder(superStr);

        builder.append(";[");
        for (int i = 0; i < nonces.length; i++) {
            builder.append(nonces[i]);
            if (i != nonces.length - 1)
                builder.append(",");
        }
        builder.append("]");
        return builder.toString();
    }

    /**
     * The String gets deserialized into a DataQueryIPFSFile object.
     *
     * @param file the String.
     * @return the DataQueryIPFSFile object.
     */
    public static DataQueryIPFSFile deserialize(String file) {
        String[] parts = file.split(";", 2);
        IPFSFile superfile = IPFSFile.deserialize(parts[0]);

        BigInteger[] nonces = Arrays.stream(
                parts[1].substring(1, parts[1].length() - 1).split(","))
                .map(str -> {if(str.equals("null"))return null; else return new BigInteger(str);}).toArray(BigInteger[]::new);

        return new DataQueryIPFSFile(superfile.getData(), nonces);
    }
}
