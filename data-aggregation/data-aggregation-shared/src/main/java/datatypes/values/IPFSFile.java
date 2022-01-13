package datatypes.values;

import encryption.KeyStore;
import io.ipfs.multihash.Multihash;
import org.bouncycastler.pqc.crypto.ntru.NTRUEncryptionPublicKeyParameters;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Collectors;

public class IPFSFile {

    private Multihash hash;
    private final String paillierKey;
    private final NTRUEncryptionPublicKeyParameters postqKey;
    private NTRUEncryptionPublicKeyParameters[] operatorKeys;
    private EncryptedData data;
    private EncryptedNonces[] nonces;

    private IPFSFile(IPFSFileBuilder builder) {
        this.hash = builder.hash;
        this.paillierKey = builder.paillierKey;
        this.postqKey = builder.postqKey;
        this.operatorKeys = builder.operatorKeys;
        this.data = builder.data;
        this.nonces = builder.nonces;
    }

    public static IPFSFile deserialize(byte[] file, int nrOperators) {
        return IPFSFile.deserialize(new String(file), nrOperators);
    }

    public static IPFSFile deserialize(String file, int nrOperators) {
        String[] parts = file.split("\n", 5);

        IPFSFileBuilder builder = new IPFSFileBuilder(parts[0], KeyStore.pqToPubKey(parts[1]));

        if (parts[2].equals("null")) return builder.build();
        String[] strOpkeys = parts[2].substring(1, parts[2].length() - 2).split(",", (nrOperators == -1) ? 0 : nrOperators);
        NTRUEncryptionPublicKeyParameters[] opKeys = new NTRUEncryptionPublicKeyParameters[strOpkeys.length];
        for (int i = 0; i < strOpkeys.length; i++)
            opKeys[i] = KeyStore.pqToPubKey(strOpkeys[i]);
        builder.setOperatorKeys(opKeys);

        if (parts[3].equals("null")) return builder.build();
        String[] noncesParts = parts[4].substring(3, parts[4].length() - 3).split("]],\\[\\[]", opKeys.length);
        EncryptedNonces[] nonces = new EncryptedNonces[noncesParts.length];
        for (int i = 0; i < noncesParts.length; i++)
            nonces[i] = new EncryptedNonces(Arrays.stream(noncesParts[i].split("],\\[", opKeys.length)).map(Base64.getDecoder()::decode).toArray(byte[][]::new));

        return builder
                .setData(EncryptedData.deserialize(parts[3]))
                .setNonces(nonces)
                .build();
    }

    public static byte[] serialize(IPFSFile file) {
        StringBuilder builder = new StringBuilder();
        builder.append(file.paillierKey)
                .append("\n").append(KeyStore.pqPubKeyToString(file.postqKey))
                .append("\n").append(file.operatorKeys == null ? "null" : Arrays.stream(file.operatorKeys).map(KeyStore::pqPubKeyToString).collect(Collectors.toList()))
                .append("\n").append(file.data == null ? "null" : file.data);

        if (file.nonces != null) {
            builder.append("\n[");
            for (int i = 0; i < file.nonces.length; i++) {
                builder.append(file.nonces[i].toString());
                if (i != file.nonces.length - 1)
                    builder.append(",");
            }
            builder.append("]");
        } else builder.append("\nnull");

        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    public Multihash getHash() {
        return hash;
    }

    public IPFSFile setHash(Multihash hash) {
        this.hash = hash;
        return this;
    }

    public String getPaillierKey() {
        return paillierKey;
    }

    public NTRUEncryptionPublicKeyParameters getPostqKey() {
        return postqKey;
    }

    public NTRUEncryptionPublicKeyParameters[] getOperatorKeys() {
        return operatorKeys;
    }

    public boolean isFull() {
        return addOperatorKey(null) == -1;
    }

    public int addOperatorKey(NTRUEncryptionPublicKeyParameters newKey) {
        for (int i = 0; i < operatorKeys.length; i++) {
            if (this.operatorKeys[i] == null) {
                this.operatorKeys[i] = newKey;
                this.hash = IPFSConnection.getInstance().addFile(this);
                return i;
            }
        }
        return -1;
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

    public int addNonces(EncryptedNonces newNonces) {
        for (int i = 0; i < this.nonces.length; i++) {
            if (!this.nonces[i].isFull()) {
                this.nonces[i] = newNonces;
                return i;
            }
        }
        return -1;
    }

    public static class IPFSFileBuilder {
        private Multihash hash;
        private final String paillierKey;
        private final NTRUEncryptionPublicKeyParameters postqKey;
        private NTRUEncryptionPublicKeyParameters[] operatorKeys;
        private EncryptedData data;
        private EncryptedNonces[] nonces;

        public IPFSFileBuilder(String paillierKey, NTRUEncryptionPublicKeyParameters postqKey) {
            this.paillierKey = paillierKey;
            this.postqKey = postqKey;
        }

        public IPFSFileBuilder setHash(Multihash hash) {
            this.hash = hash;
            return this;
        }

        public IPFSFileBuilder setOperatorKeys(NTRUEncryptionPublicKeyParameters[] operatorKeys) {
            this.operatorKeys = operatorKeys;
            return this;
        }

        public IPFSFileBuilder setData(EncryptedData data) {
            this.data = data;
            return this;
        }

        public IPFSFileBuilder setNonces(EncryptedNonces[] nonces) {
            this.nonces = nonces;
            return this;
        }

        public IPFSFile build() {
            IPFSFile file = new IPFSFile(this);
            System.out.println("Made the actual object");
            if (file.hash == null) file.hash = IPFSConnection.getInstance().addFile(file);
            System.out.println("Set the hash");
            return file;
        }
    }
}

