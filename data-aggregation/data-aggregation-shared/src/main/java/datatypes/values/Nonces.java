package datatypes.values;

import datatypes.aggregationprocess.AggregationProcess;

import java.math.BigInteger;
import java.util.ArrayList;

public class Nonces {

    public static BigInteger condenseNonces(BigInteger[] nonces) {
        BigInteger res = new BigInteger("0");
        for(BigInteger nonce: nonces) {
            res = res.add(nonce);
        }
        return res;
    }

    public static BigInteger[] getOperatorNonces(int index, ArrayList<BigInteger[]> nonces) {
        BigInteger[] res = new BigInteger[nonces.size()];
        for (int i = 0; i < nonces.size(); i++) {
            res[i] = nonces.get(i)[index];
        }
        return res;
    }
}
