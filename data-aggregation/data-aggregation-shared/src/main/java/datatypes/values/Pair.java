package datatypes.values;

public class Pair<C, E> {

    private C p1;
    private E p2;

    public Pair(C p1, E p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    public C getP1() {
        return p1;
    }

    public void setP1(C p1) {
        this.p1 = p1;
    }

    public E getP2() {
        return p2;
    }

    public void setP2(E p2) {
        this.p2 = p2;
    }

    @Override
    public String toString() {
        return "( " + p1.toString() + " , " + p2.toString() + " )";
    }
}
