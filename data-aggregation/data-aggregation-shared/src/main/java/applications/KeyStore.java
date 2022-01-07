package applications;

import com.n1analytics.paillier.PaillierPublicKey;
import org.bouncycastler.pqc.crypto.ntru.NTRUEncryptionParameters;
import org.bouncycastler.pqc.crypto.ntru.NTRUEncryptionPublicKeyParameters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Base64;

public interface KeyStore {



    public static String paPubKeyToString(PaillierPublicKey pk) {
        return pk.getModulus().toString();
    }

    public static PaillierPublicKey paStringToPubKey(String str) {
        return new PaillierPublicKey(new BigInteger(str));
    }

    public static String pqPubKeyToString(NTRUEncryptionPublicKeyParameters pk) {
        ByteArrayOutputStream pubOut = new ByteArrayOutputStream();
        ByteArrayOutputStream parOut = new ByteArrayOutputStream();
        try {
            pk.writeTo(pubOut);
            pk.getParameters().writeTo(parOut);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String pubKey = Base64.getEncoder().encodeToString(pubOut.toByteArray());
        String stParams = Base64.getEncoder().encodeToString(parOut.toByteArray());
        return pubKey + ":" + stParams;
    }

    public static NTRUEncryptionPublicKeyParameters pqStringToPubKey(String str) {
        String[] parts = str.split(":", 2);
        byte[] pubKey = Base64.getDecoder().decode(parts[0]);
        byte[] stParams = Base64.getDecoder().decode(parts[1]);
        NTRUEncryptionParameters params = null;

        try {
            params = new NTRUEncryptionParameters(new ByteArrayInputStream(stParams));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new NTRUEncryptionPublicKeyParameters(pubKey, params);
    }
}
