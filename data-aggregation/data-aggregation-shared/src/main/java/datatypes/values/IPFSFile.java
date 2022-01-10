package datatypes.values;

import io.ipfs.multihash.Multihash;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

public class IPFSFile {

    private Multihash hash;
    private String paillierKey;
    private byte[] postqKey;
    private byte[][] operatorKeys;
    private EncryptedData data;
    private EncryptedNonces[] nonces;

    public IPFSFile() {

    }

    public IPFSFile(Multihash hash, String paillierKey, byte[] postqKey, byte[][] operatorKeys, EncryptedData data, EncryptedNonces[] nonces) {
        this.hash = hash;
        this.paillierKey = paillierKey;
        this.postqKey = postqKey;
        this.operatorKeys = operatorKeys;
        this.data = data;
        this.nonces = nonces;
    }

    public IPFSFile(Multihash hash) {
        IPFSFile file = null;
        try {
            file = IPFSFile.deserialize(IPFSConnection.getInstance().getIpfs().cat(hash));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.hash = hash;
        this.paillierKey = file.paillierKey;
        this.postqKey = file.postqKey;
        this.operatorKeys = file.operatorKeys;
        this.data = file.data;
        this.nonces = file.nonces;
    }

    public static IPFSFile deserialize(byte[] file) {
        return IPFSFile.deserialize(new String(file));
    }

    public static IPFSFile deserialize(byte[] file, int nrOperators, int nrParticipants) {
        return IPFSFile.deserialize(new String(file), nrOperators, nrParticipants);
    }

    public static IPFSFile deserialize(String file, int nrOperators, int nrParticipants) {
        String[] parts = file.split("\n", 5);

        byte[] postqKey = Base64.getDecoder().decode(parts[1]);

        String[] strOpkeys = parts[2].substring(1, parts[2].length() - 2).split(",", nrOperators);
        byte[][] opKeys = new byte[strOpkeys.length][];
        for (int i = 0; i < strOpkeys.length; i++)
            opKeys[i] = Base64.getDecoder().decode(strOpkeys[i]);

        String[] noncesParts = parts[5].substring(3, parts[5].length() - 3).split("]],\\[\\[]", nrOperators);
        EncryptedNonces[] nonces = new EncryptedNonces[nrParticipants];
        for (int i = 0, noncesPartsLength = noncesParts.length; i < noncesPartsLength; i++) {
            nonces[i] = new EncryptedNonces(Arrays.stream(noncesParts[i].split("],\\[", nrOperators)).map(Base64.getDecoder()::decode).toArray(byte[][]::new));
        }
        IPFSFile ipfsFile = new IPFSFile()
                .setPaillierKey(parts[0])
                .setPostqKey(postqKey)
                .setOperatorKeys(opKeys)
                .setData(EncryptedData.deserialize(parts[4]))
                .setNonces(nonces);
        ipfsFile.setHash(IPFSConnection.getInstance().addFile(ipfsFile));
        return ipfsFile;
    }

    public static IPFSFile deserialize(String file) {
        String[] parts = file.split("\n", 5);

        byte[] postqKey = Base64.getDecoder().decode(parts[1]);

        String[] strOpkeys = parts[2].substring(1, parts[2].length() - 2).split(",");
        byte[][] opKeys = new byte[strOpkeys.length][];
        for (int i = 0; i < strOpkeys.length; i++)
            opKeys[i] = Base64.getDecoder().decode(strOpkeys[i]);

        String[] noncesParts = parts[5].substring(3, parts[5].length() - 3).split("]],\\[\\[]");
        EncryptedNonces[] nonces = new EncryptedNonces[noncesParts.length];
        for (int i = 0, noncesPartsLength = noncesParts.length; i < noncesPartsLength; i++) {
            nonces[i] = new EncryptedNonces(Arrays.stream(noncesParts[i].split("],\\[", opKeys.length)).map(Base64.getDecoder()::decode).toArray(byte[][]::new));
        }
        IPFSFile ipfsFile = new IPFSFile()
                .setPaillierKey(parts[0])
                .setPostqKey(postqKey)
                .setOperatorKeys(opKeys)
                .setData(EncryptedData.deserialize(parts[4]))
                .setNonces(nonces);
        ipfsFile.setHash(IPFSConnection.getInstance().addFile(ipfsFile));
        return ipfsFile;
    }

    public static byte[] serialize(IPFSFile file) {
        StringBuilder builder = new StringBuilder();
        builder.append("\n").append(file.paillierKey)
                .append("\n").append(file.postqKey)
                .append("\n").append(Arrays.toString(file.getOperatorKeys()))
                .append("\n").append(file.data)
                .append("\n[");
        for (int i = 0; i < file.nonces.length; i++) {
            builder.append(file.nonces[i].toString());
            if (i != file.nonces.length - 1)
                builder.append(",");
        }
        builder.append("]");
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    public String getPaillierKey() {
        return paillierKey;
    }

    public IPFSFile setPaillierKey(String paillierKey) {
        this.paillierKey = paillierKey;
        return this;
    }

    public byte[] getPostqKey() {
        return postqKey;
    }

    public IPFSFile setPostqKey(byte[] postqKey) {
        this.postqKey = postqKey;
        return this;
    }

    public byte[][] getOperatorKeys() {
        return operatorKeys;
    }

    public IPFSFile setOperatorKeys(byte[][] operatorKeys) {
        this.operatorKeys = operatorKeys;
        return this;
    }

    public EncryptedData getData() {
        return data;
    }

    public IPFSFile setData(EncryptedData data) {
        this.data = data;
        return this;
    }

    public EncryptedNonces[] getNonces() {
        return nonces;
    }

    public IPFSFile setNonces(EncryptedNonces[] nonces) {
        this.nonces = nonces;
        return this;
    }

    public Multihash getHash() {
        return hash;
    }

    public IPFSFile setHash(Multihash hash) {
        this.hash = hash;
        return this;
    }
}

