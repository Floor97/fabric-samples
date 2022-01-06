//import com.n1analytics.paillier.PaillierPrivateKey;
//import com.n1analytics.paillier.PaillierPublicKey;
//import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
//import org.bouncycastle.pqc.crypto.ntru.*;
//import shared.Pair;
//
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.math.BigInteger;
//import java.util.Base64;
//
//public class Keystore {
//
//    private PaillierPrivateKey paillierKeys;
//    private AsymmetricCipherKeyPair postQuantumKeys;
//
//    public PaillierPrivateKey getPaillierKeys() {
//        return this.paillierKeys;
//    }
//
//    public Keystore setPaillierKeys() {
//        paillierKeys = PaillierPrivateKey.create(64);
//        return this;
//    }
//
//    public AsymmetricCipherKeyPair getPostQuantumKeys() {
//        return this.postQuantumKeys;
//    }
//
//    public Pair<PaillierPublicKey,NTRUEncryptionPublicKeyParameters> getPublicKeys() {
//        return new Pair(paillierKeys.getPublicKey(), postQuantumKeys.getPublic());
//    }
//
//    public Keystore setPostQuantumKeys() {
//        NTRUEncryptionKeyGenerationParameters params = NTRUEncryptionKeyGenerationParameters.APR2011_743_FAST.clone();
//        params.polyType = NTRUParameters.TERNARY_POLYNOMIAL_TYPE_SIMPLE;
//        NTRUEncryptionKeyPairGenerator ntruGen = new NTRUEncryptionKeyPairGenerator();
//        ntruGen.init(params);
//        this.postQuantumKeys = ntruGen.generateKeyPair();
//        return this;
//    }
//
//    public static Keystore createInstance() {
//        return new Keystore().setPaillierKeys().setPostQuantumKeys();
//    }
//
//
//
//    public static String paPubKeyToString(PaillierPublicKey pk) {
//        return pk.getModulus().toString();
//    }
//
//    public static PaillierPublicKey paStringToPubKey(String str) {
//        return new PaillierPublicKey(new BigInteger(str));
//    }
//
//    public static String pqPubKeyToString(NTRUEncryptionPublicKeyParameters pk) {
//        ByteArrayOutputStream pubOut = new ByteArrayOutputStream();
//        ByteArrayOutputStream parOut = new ByteArrayOutputStream();
//        try {
//            pk.writeTo(pubOut);
//            pk.getParameters().writeTo(parOut);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        String pubKey = Base64.getEncoder().encodeToString(pubOut.toByteArray());
//        String stParams = Base64.getEncoder().encodeToString(parOut.toByteArray());
//        return pubKey + ":" + stParams;
//    }
//
//    public static NTRUEncryptionPublicKeyParameters pqStringToPubKey(String str) {
//        String[] parts = str.split(":", 2);
//        byte[] pubKey = Base64.getDecoder().decode(parts[0]);
//        byte[] stParams = Base64.getDecoder().decode(parts[1]);
//        NTRUEncryptionParameters params = null;
//
//        try {
//            params = new NTRUEncryptionParameters(new ByteArrayInputStream(stParams));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return new NTRUEncryptionPublicKeyParameters(pubKey, params);
//    }
//}
