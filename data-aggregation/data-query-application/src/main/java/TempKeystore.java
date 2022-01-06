import com.n1analytics.paillier.PaillierPrivateKey;
import com.n1analytics.paillier.PaillierPublicKey;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import shared.Pair;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Base64;

public class TempKeystore {

    private PaillierPrivateKey paillierKeys;
    private String postQuantumKeys;

    public PaillierPrivateKey getPaillierKeys() {
        return this.paillierKeys;
    }

    public TempKeystore setPaillierKeys() {
        paillierKeys = PaillierPrivateKey.create(64);
        return this;
    }

    public String getPostQuantumKeys() {
        return this.postQuantumKeys;
    }

    public Pair<PaillierPublicKey,String> getPublicKeys() {
        return new Pair(paillierKeys.getPublicKey(), postQuantumKeys);
    }

    public TempKeystore setPostQuantumKeys() {
        this.postQuantumKeys = "ABgAAAYAgAH0v//9HwAABjCA/g+g//0fwAD6DwAA9L//ABjAAAAAgP4DoP8DAAAA+t9/AAAAAAMYQP/7P4D+DwAA/R/AAAAwgP73HwAAGMAABgCAAfQfAAMYQP/7D4D+AwAA/e//AAAAgP73HwAA6P8A+g+AAfR/AADoPwAG0H8A9L///QdA/wHQ//4PYAD9HwAAAND/AQCg/wAYQP8BAID+A2AA/e//APoPAAAAoP8A6D8AAAAAAABgAAMYQP/7P4ABDGAA/QcAAAYwgP4DoP8A6H//+z8AAACg//0fAAAGAID+D2AAAOh//wcwAAAAYAADAAAA+g+A/gNgAADo/wD63/8B9B8AAAAAAAYwgP4PAAD9H8AAAND//vd/AADo/wAGAIAB9L//AABA/wEwgAEAYAAA6H//B9B/AAxgAAAYwAAGAIABAAAA/e//APo/AAD0v/8A6P8A+t9/AABgAP0fwAAGAIABDKD/AxjAAAYwAAAAAAAAAAAA+g+AAQCg//0fwAD6D4D+D2AAAOh//wcAgP4PYAD9BwAAAND/AQBgAP0fwAAAAID+938A/e//AAbQ/wEMAAAAAMAAADCA/gOg/wDoPwAA0H8AAGAAAAAAAPrf//4PoP8DGMAABgAAAPS//wDof/8HMIABDGAAAwBA//s/gAH0v/8DGMAAAAAAAABgAAAAAAAA0H8A9H8AAwDAAPo/AAAAYAAAGAAABgAAAABgAP3vPwAGMID+A6D/A+j/AAbQ/wH0fwAAGAAA+t///ve///3vf//7D4AB9H8A/R/AAAAAAAAMAAADAAAA+j+AAQCg/wPoPwAAMAAAAGAAAwAAAAbQfwAMoP/97z8AAND/AfS///3v/wD6P4ABAGAA/R9A/wEAgAEAYAAAGMAAAND/AQCg/wDo/wAGMAAA9L///e9//wHQfwAAAAAAGED/+w+AAQyg//3vf/8HAIAB9H8AAADAAPrffwD0v/8D6H//AdD//vcfAP0fwAAA0P8B9L//AxgAAAYwgAEMAAD9B0D/+w8AAAxgAP0HwAAA0P/+A2AAAABA/wEwgAEAYAADGMAA+g8AAAwAAP3v/wD6D4D+D6D//R/AAAAwgP73HwD9H0D/BwCA/g+g//0HwAAGMAAADGAAA+g/AAAAgAH0fwD97z8AADCA/vcfAP0HAAD6D4D+D6D/ABhA/wcAAAAAAAAD6D8ABgCAAfQfAADof/8HAIABDKD//QfAAPrffwAMoP/97/8AAACAAfQfAP0fwAAA0H8ADGAA/R8AAAAwAAAAAAD9HwAAANB/AACg/wMAAAAAAID+938AAOj/AADQfwD0v//9H8AABgCAAQBgAAAAQP/7P4D+DwA=:AAAC5wAACAAAAAAAAAAAAAAAAAAAAAAAAAABAAAAANwAAAAKAAAAGwAAAA4BAAdpAAEAAAdTSEEtNTEy";
        return this;
    }

    public static TempKeystore createInstance() {
        return new TempKeystore().setPaillierKeys().setPostQuantumKeys();
    }



    public static String paPubKeyToString(PaillierPublicKey pk) {
        return pk.getModulus().toString();
    }

    public static PaillierPublicKey paStringToPubKey(String str) {
        return new PaillierPublicKey(new BigInteger(str));
    }

    public static String pqPubKeyToString(String pk) {
        return pk;
    }

    public static String pqStringToPubKey(String str) {
        return str;
    }
}
