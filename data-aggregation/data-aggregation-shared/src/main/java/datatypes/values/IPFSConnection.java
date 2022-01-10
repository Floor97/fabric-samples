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

    public static IPFSConnection getInstance() {
        if(IPFSConnection.ipfsConnection == null) ipfsConnection = new IPFSConnection();
        return ipfsConnection;
    }

    public IPFS getIpfs() {
        return ipfs;
    }

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

    public IPFSFile getFile(String hash) {
        return IPFSConnection.getInstance().getFile(Multihash.fromHex(hash));
    }

    public IPFSFile getFile(Multihash hash) {
        byte[] file = null;
        try {
            file = ipfs.cat(hash);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return IPFSFile.deserialize(file, -1);
    }
}
