package datatypes.values;

import encryption.NTRUEncryption;
import io.ipfs.multihash.Multihash;
import org.bouncycastler.pqc.crypto.ntru.NTRUEncryptionPublicKeyParameters;

import java.io.IOException;


public class IPFSFile {

    private Multihash hash;
    private EncryptedData data;
    private final String paillierKey;
    private final NTRUEncryptionPublicKeyParameters postqKey;

    public IPFSFile(String paillierKey, NTRUEncryptionPublicKeyParameters postqKey) {
        this.paillierKey = paillierKey;
        this.postqKey = postqKey;
    }

    public IPFSFile(String paillierKey, NTRUEncryptionPublicKeyParameters postqKey, EncryptedData data) {
        this.paillierKey = paillierKey;
        this.postqKey = postqKey;
        this.data = data;
    }

    public Multihash getHash() throws IOException {
        if (hash == null) createHash();
        return hash;
    }

    public void createHash() throws IOException {
        this.hash = IPFSConnection.getInstance().addFile(this);
    }

    /**
     * Deserializes the IPFSFile object.
     *
     * @param file the deserialized IPFSfile.
     * @return the IPFSFile.
     */
    public static IPFSFile deserialize(String file) {
        String[] parts = file.split("\n", 3);
        return new IPFSFile(parts[0], NTRUEncryption.deserialize(parts[1]))
                .setData(EncryptedData.deserialize(parts[2]));
    }

    /**
     * Serializes the IPFSFile object.
     *
     * @return the serialized IPFSFile object.
     */
    public String serialize() {
        return this.paillierKey + "\n" +
                NTRUEncryption.serialize(this.postqKey) + "\n" +
                data.serialize();
    }

    public String getPaillierKey() {
        return paillierKey;
    }

    public NTRUEncryptionPublicKeyParameters getPostqKey() {
        return postqKey;
    }

    public EncryptedData getData() {
        return data;
    }

    public IPFSFile setData(EncryptedData data) {
        this.data = data;
        return this;
    }
}

