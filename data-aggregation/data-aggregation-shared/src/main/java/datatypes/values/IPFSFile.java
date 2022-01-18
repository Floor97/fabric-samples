package datatypes.values;

import io.ipfs.multihash.Multihash;

import java.io.IOException;
import java.math.BigInteger;


public class IPFSFile {

    private Multihash hash;
    private BigInteger data;

    public IPFSFile(BigInteger data) {
        this.data = data;
    }

    public Multihash getHash() throws IOException {
        createHash();
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
        return new IPFSFile(new BigInteger(file));
    }

    /**
     * Serializes the IPFSFile object.
     *
     * @return the serialized IPFSFile object.
     */
    public String serialize() {
        return data.toString();
    }

    public BigInteger getData() {
        return data;
    }

    public IPFSFile setData(BigInteger data) {
        this.data = data;
        return this;
    }
}

