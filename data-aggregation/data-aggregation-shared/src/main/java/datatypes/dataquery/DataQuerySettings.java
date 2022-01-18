package datatypes.dataquery;

public class DataQuerySettings {

    private final int nrOperators;
    private final int nrExpectedParticipants;
    private final long duration;

    public DataQuerySettings(int nrOperators, int nrExpectedParticipants, long duration) {
        this.nrOperators = nrOperators;
        this.nrExpectedParticipants = nrExpectedParticipants;
        this.duration = duration;
    }

    public int getNrOperators() {
        return nrOperators;
    }

    public int getNrExpectedParticipants() {
        return this.nrExpectedParticipants;
    }

    public long getDuration() {
        return duration;
    }

    @Override
    public String toString() {
        return "    number of operators: " + this.nrOperators +
                ",\n    expected number of participants: " + this.nrExpectedParticipants +
                ",\n    duration: " + this.duration;
    }
}
