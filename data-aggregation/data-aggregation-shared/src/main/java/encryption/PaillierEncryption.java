package encryption;

import com.n1analytics.paillier.EncryptedNumber;
import com.n1analytics.paillier.PaillierContext;
import com.n1analytics.paillier.PaillierPrivateKey;
import com.n1analytics.paillier.PaillierPublicKey;
import datatypes.values.EncryptedData;

import java.math.BigInteger;

public class PaillierEncryption {

    public static EncryptedData encrypt(BigInteger data, String modulus) {
        PaillierPublicKey reformedPublicKey = new PaillierPublicKey(new BigInteger(modulus));
        PaillierContext ctx = reformedPublicKey.createUnsignedContext();
        EncryptedNumber encData = ctx.encrypt(data);
        return new EncryptedData(encData.calculateCiphertext().toString(), String.valueOf(encData.getExponent()));
    }

    public static BigInteger decrypt(EncryptedData data, PaillierPrivateKey privateKey) {
        PaillierPublicKey pubkey = privateKey.getPublicKey();
        PaillierContext ctx = pubkey.createUnsignedContext();

        EncryptedNumber encryptedNumber = new EncryptedNumber(ctx, new BigInteger(data.getData()), Integer.parseInt(data.getExponent()));
        return privateKey.decrypt(encryptedNumber).decodeBigInteger();
    }
}
