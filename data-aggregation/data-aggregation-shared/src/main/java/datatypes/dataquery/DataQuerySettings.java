package datatypes.dataquery;

public class DataQuerySettings {

    private final int nrOperators;
    private final long duration;

    public DataQuerySettings(int nrOperators, long duration) {
        this.nrOperators = nrOperators;
        this.duration = duration;
    }

    public int getNrOperators() {
        return nrOperators;
    }

    public long getDuration() {
        return duration;
    }

    @Override
    public String toString() {
        return "    number of operators: " + this.nrOperators
                + ",\n    duration: " + this.duration;
    }
}
