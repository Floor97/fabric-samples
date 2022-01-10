package datatypes.dataquery;

public class DataQuerySettings {

    private int nrOperators;
    private long duration;

    public int getNrOperators() {
        return nrOperators;
    }

    public DataQuerySettings setNrOperators(int nrOperators) {
        this.nrOperators = nrOperators;
        return this;
    }

    public long getDuration() {
        return duration;
    }

    public DataQuerySettings setDuration(long duration) {
        this.duration = duration;
        return this;
    }

    /**
     * Factory method for DataQuerySettings.
     * @param nrOperators the number of operators used in the process.
     * @param endTime the end time of the process.
     * @return a new DataQuerySettings object.
     */
    public static DataQuerySettings createInstance(int nrOperators, long endTime) {
        return new DataQuerySettings()
                .setNrOperators(nrOperators)
                .setDuration(endTime);
    }

    @Override
    public String toString() {
        return  "    number of operators: " + this.nrOperators
                + ",\n    duration: " + this.duration;
    }
}
