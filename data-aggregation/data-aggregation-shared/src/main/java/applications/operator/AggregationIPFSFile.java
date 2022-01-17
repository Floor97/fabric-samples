package applications.operator;

import datatypes.values.EncryptedData;
import datatypes.values.EncryptedNonce;
import datatypes.values.EncryptedNonces;
import datatypes.values.IPFSFile;
import encryption.NTRUEncryption;
import org.bouncycastler.pqc.crypto.ntru.NTRUEncryptionPublicKeyParameters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class AggregationIPFSFile extends IPFSFile {

    private NTRUEncryptionPublicKeyParameters[] operatorKeys;
    private ArrayList<EncryptedNonces> nonces;

    public AggregationIPFSFile(String paillierKey, NTRUEncryptionPublicKeyParameters postqKey, EncryptedData data, NTRUEncryptionPublicKeyParameters[] operatorKeys, ArrayList<EncryptedNonces> nonces) {
        super(paillierKey, postqKey, data);
        this.operatorKeys = operatorKeys;
        this.nonces = nonces;
    }

    /**
     * An EncryptedNonces is added to the nonces ArrayList.
     *
     * @param newNonces the EncryptedNonces object.
     */
    public void addNonces(EncryptedNonces newNonces) {
        this.nonces.add(newNonces);
    }

    /**
     * A new public key is added to the operatorKeys array.
     *
     * @param newKey the public key used for NTRUEncrypt.
     * @return the index at which the key was inserted. -1 if the array is full.
     * @throws IOException thrown if an IOException occurs adding a file to the IPFS network.
     */
    public int addOperatorKey(NTRUEncryptionPublicKeyParameters newKey) throws IOException {
        for (int i = 0; i < operatorKeys.length; i++) {
            if (this.operatorKeys[i] == null) {
                if (newKey != null) {
                    this.operatorKeys[i] = newKey;
                    this.createHash();
                }
                return i;
            }
        }
        return -1;
    }

    /**
     * Checks if the operatorKeys array is full.
     *
     * @return true if the array is full, otherwise false.
     * @throws IOException thrown if an IOException occurs adding a file to the IPFS network.
     */
    public boolean isFull() throws IOException {
        return addOperatorKey(null) == -1;
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

        String[] strOpkeys = parts[1].substring(1, parts[1].length() - 1).split(",");
        NTRUEncryptionPublicKeyParameters[] opKeys = Arrays.stream(strOpkeys)
                .map(NTRUEncryption::deserialize)
                .toArray(NTRUEncryptionPublicKeyParameters[]::new);

        ArrayList<EncryptedNonces> nonces = new ArrayList<>();

        if (!parts[0].equals("[]")) {
            String[] noncesParts = parts[0].substring(2, parts[0].length() - 2).split("],\\[", opKeys.length);
            for (String nonce : noncesParts)
                nonces.add(new EncryptedNonces(Arrays.stream(nonce.split(",", opKeys.length)).map(EncryptedNonce::deserialize).toArray(EncryptedNonce[]::new)));
        }

        return new AggregationIPFSFile(superfile.getPaillierKey(), superfile.getPostqKey(), superfile.getData(), opKeys, nonces);
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
                    .append(Arrays.stream(this.nonces.get(i).getNonces())
                            .map(EncryptedNonce::toString)
                            .collect(Collectors.joining(",")))
                    .append("]");
            if (i != this.nonces.size() - 1)
                builder.append(",");
        }
        builder.append("]\n")
                .append(Arrays.stream(this.operatorKeys)
                        .map(NTRUEncryption::serialize)
                        .collect(Collectors.toList()));

        return builder.toString();
    }

    public ArrayList<EncryptedNonces> getNonces() {
        return nonces;
    }

    public NTRUEncryptionPublicKeyParameters[] getOperatorKeys() {
        return operatorKeys;
    }
}
