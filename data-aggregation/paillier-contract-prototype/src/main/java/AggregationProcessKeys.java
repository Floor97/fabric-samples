import java.util.Arrays;

public class AggregationProcessKeys {

    private String paillierModulus;
    private String[] operatorKeys;
    private int pointer = 0;

    public String getPaillierModulus() {
        return paillierModulus;
    }

    public AggregationProcessKeys setPaillierModulus(String paillierModulus) {
        this.paillierModulus = paillierModulus;
        return this;
    }

    public String[] getOperatorKeys() {
        return operatorKeys;
    }

    public AggregationProcessKeys addOperatorKey(String operatorKey) {
        if(isOperatorKeysFull()) throw new RuntimeException("Operator keys is full");
        this.operatorKeys[pointer++] = operatorKey;
        return this;
    }

    public boolean isOperatorKeysFull() {
        return this.pointer >= this.operatorKeys.length;
    }

    public AggregationProcessKeys setOperatorKeys(String[] operatorKeys) {
        this.operatorKeys = operatorKeys;
        this.pointer = 0;
        for (String operatorKey : operatorKeys) {
            if (operatorKey == null)
                break;
            pointer++;
        }
        return this;
    }

    /**
     * Factory method of AggregationProcessKeys.
     * @param paillierModulus the modulus of the paillier public key.
     * @param nrOperators the number of operators involved in the process.
     * @return The AggregationProcessKeys object.
     */
    public static AggregationProcessKeys createInstance(String paillierModulus, int nrOperators) {
        return new AggregationProcessKeys()
                .setPaillierModulus(paillierModulus)
                .setOperatorKeys(new String[nrOperators]);
    }

    @Override
    public String toString() {
        return "    paillier Modulus: " + this.paillierModulus +
                ",\n    number of assigned operators: " + this.pointer +
                ",\n    keys of operators: " + Arrays.toString(this.operatorKeys);
    }
}
