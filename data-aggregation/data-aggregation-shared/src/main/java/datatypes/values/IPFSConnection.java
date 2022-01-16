package datatypes.values;

import io.ipfs.api.IPFS;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;
import io.ipfs.multihash.Multihash;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class IPFSConnection {

    private static IPFSConnection ipfsConnection = null;

    private final IPFS ipfs;

    private IPFSConnection() {
        this.ipfs = new IPFS("/ip4/192.168.0.106/tcp/5001");
        try {
            ipfs.refs.local();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public IPFS getIpfs() {
        return ipfs;
    }

    /**
     * Adds a new file to be hosted on IPFS.
     *
     * @param serFile the IPFS file.
     * @return the hash of the new file.
     */
    public Multihash addFile(IPFSFile serFile) throws IOException {
        NamedStreamable.ByteArrayWrapper file = new NamedStreamable.ByteArrayWrapper(serFile.serialize().getBytes(StandardCharsets.UTF_8));
        MerkleNode addResult = ipfs.add(file).get(0);
        return addResult.hash;
    }

    /**
     * Retrieves a file from IPFS using the hash.
     *
     * @param hash the hash associated with the file.
     * @return the IPFS file.
     */
    public DataQueryIPFSFile getDataQueryIPFSFile(Multihash hash) throws IOException {
        byte[] file = ipfs.cat(hash);
        return DataQueryIPFSFile.deserialize(new String(file));
    }

    /**
     * Retrieves a file from IPFS using the hash.
     *
     * @param hash the hash associated with the file.
     * @return the IPFS file.
     */
    public AggregationIPFSFile getAggregationIPFSFile(Multihash hash) throws IOException {
        byte[] file = ipfs.cat(hash);
        return AggregationIPFSFile.deserialize(new String(file));
    }

    public static IPFSConnection getInstance() {
        if (IPFSConnection.ipfsConnection == null) ipfsConnection = new IPFSConnection();
        return ipfsConnection;
    }

}
