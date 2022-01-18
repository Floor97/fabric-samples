package applications.operator;

import datatypes.values.IPFSFile;
import org.bouncycastler.pqc.crypto.ntru.NTRUEncryptionPublicKeyParameters;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class AggregationIPFSFile extends IPFSFile {

    private final ArrayList<BigInteger[]> nonces;

    public AggregationIPFSFile(BigInteger data, ArrayList<BigInteger[]> nonces) {
        super(data);
        this.nonces = nonces;
    }

    /**
     * A nonce is added to the nonces ArrayList.
     *
     * @param nonce the EncryptedNonces object.
     */
    public void addNonces(BigInteger[] nonce) {
        this.nonces.add(nonce);
    }

    /**
     * Deserializes String into a AggregationIPFSFile object.
     *
     * @param file the String.
     * @return the AggregationIPFSFile.
     */
    public static AggregationIPFSFile deserialize(String file) {
        String[] superParts = file.split(";", 2);
        IPFSFile superfile = IPFSFile.deserialize(superParts[0]);

        String[] parts = superParts[1].split("\n", 2);

        ArrayList<BigInteger[]> nonces = new ArrayList<>();

        if (!parts[0].equals("[]")) {
            String[] noncesParts = parts[0].substring(2, parts[0].length() - 2).split("],\\[");
            for (String nonce : noncesParts)
                nonces.add(Arrays.stream(nonce.split(",")).map(BigInteger::new).toArray(BigInteger[]::new));
        }

        return new AggregationIPFSFile(superfile.getData(), nonces);
    }

    /**
     * Serializes the AggregationIPFSFile into a String.
     *
     * @return the String.
     */
    public String serialize() {
        String superStr = super.serialize();
        StringBuilder builder = new StringBuilder(superStr);

        builder.append(";[");
        for (int i = 0; i < this.nonces.size(); i++) {
            builder.append("[")
                    .append(Arrays.stream(this.nonces.get(i))
                            .map(BigInteger::toString)
                            .collect(Collectors.joining(",")))
                    .append("]");
            if (i != this.nonces.size() - 1)
                builder.append(",");
        }
        builder.append("]");
        return builder.toString();
    }

    public ArrayList<BigInteger[]> getNonces() {
        return nonces;
    }
}
