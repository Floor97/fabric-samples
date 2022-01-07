package applications.operator.generators;

import com.n1analytics.paillier.EncryptedNumber;
import com.n1analytics.paillier.PaillierContext;
import com.n1analytics.paillier.PaillierPrivateKey;
import com.n1analytics.paillier.PaillierPublicKey;
import shared.Pair;

import java.math.BigInteger;

public class PaillierEncryption {

    public static Pair<String, Integer> encrypt(BigInteger data, String modulus) {
        PaillierPublicKey reformedPublicKey = new PaillierPublicKey(new BigInteger(modulus));
        PaillierContext ctx = reformedPublicKey.createUnsignedContext();
        EncryptedNumber encData = ctx.encrypt(data);
        return new Pair<>(encData.calculateCiphertext().toString(), encData.getExponent());
    }

    public static BigInteger decrypt(String data, String exponent, PaillierPrivateKey privateKey) {
        PaillierPublicKey pubkey = privateKey.getPublicKey();
        PaillierContext ctx = pubkey.createUnsignedContext();

        EncryptedNumber encryptedNumber = new EncryptedNumber(ctx, new BigInteger(data), Integer.parseInt(exponent));
        return privateKey.decrypt(encryptedNumber).decodeBigInteger();
    }
}
