package datatypes.values;


import io.ipfs.multihash.Multihash;
import org.bouncycastler.pqc.crypto.ntru.NTRUEncryptionPublicKeyParameters;

import java.util.Arrays;

public class DataQueryIPFSFile extends IPFSFile {

    private EncryptedNonces nonces;

    public DataQueryIPFSFile(String paillierKey, NTRUEncryptionPublicKeyParameters postqKey) {
        super(paillierKey, postqKey);
    }

    public DataQueryIPFSFile(String paillierKey, NTRUEncryptionPublicKeyParameters postqKey, EncryptedData data, EncryptedNonces nonces) {
        super(paillierKey, postqKey, data);
        this.nonces = nonces;
    }

    public EncryptedNonces getNonces() {
        return nonces;
    }

    public void addNonce(EncryptedNonce nonce) {
        this.nonces.addNonce(nonce);
    }

    public String serialize() {
        String superStr = super.serialize();
        StringBuilder builder = new StringBuilder(superStr);

        builder.append(";[");
        EncryptedNonce[] nonces = this.nonces.getNonces();
        for (int i = 0; i < nonces.length; i++) {
            builder.append(nonces[i] == null ? null : nonces[i].toString());
            if (i != nonces.length - 1)
                builder.append(",");
        }
        builder.append("]");
        return builder.toString();
    }

    public static DataQueryIPFSFile deserialize(String file) {
        String[] parts = file.split(";", 2);
        IPFSFile superfile = IPFSFile.deserialize(parts[0]);

        EncryptedNonces nonces = new EncryptedNonces(Arrays.stream(
                parts[1].substring(1, parts[1].length() - 1).split(",")
        ).map(EncryptedNonce::deserialize).toArray(EncryptedNonce[]::new));

        return new DataQueryIPFSFile(superfile.getPaillierKey(), superfile.getPostqKey(), superfile.getData(), nonces);
    }
}
