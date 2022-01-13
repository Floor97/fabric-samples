package datatypes.values;

import io.ipfs.api.IPFS;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;
import io.ipfs.multihash.Multihash;

import java.io.IOException;

public class IPFSConnection {

    private static IPFSConnection ipfsConnection = null;

    private final IPFS ipfs;

    private IPFSConnection() {
        this.ipfs = new IPFS("/ip4/127.0.0.1/tcp/5001");
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
    public Multihash addFile(IPFSFile serFile) {
        NamedStreamable.ByteArrayWrapper file = new NamedStreamable.ByteArrayWrapper(IPFSFile.serialize(serFile));
        MerkleNode addResult = null;
        try {
            addResult = ipfs.add(file).get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return addResult.hash;
    }

    /**
     * Retrieves a file from IPFS using the hash.
     *
     * @param hash the hash associated with the file.
     * @return the IPFS file.
     */
    public IPFSFile getFile(String hash) {
        return IPFSConnection.getInstance().getFile(Multihash.fromHex(hash));
    }

    /**
     * Retrieves a file from IPFS using the hash.
     *
     * @param hash the hash associated with the file.
     * @return the IPFS file.
     */
    public IPFSFile getFile(Multihash hash) {
        byte[] file = null;
        try {
            file = ipfs.cat(hash);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return IPFSFile.deserialize(file, -1);
    }

    public static IPFSConnection getInstance() {
        if (IPFSConnection.ipfsConnection == null) ipfsConnection = new IPFSConnection();
        return ipfsConnection;
    }

}
