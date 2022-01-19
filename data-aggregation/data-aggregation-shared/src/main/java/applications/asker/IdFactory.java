package applications.asker;

public class IdFactory {

    private static IdFactory idFactory = null;
    private int counter;
    private String askerName;


    private IdFactory() {
        this.counter = 0;
    }

    public String getAskerName() {
        return this.askerName;
    }

    /**
     * Sets the name of the asker and resets the counter.
     *
     * @param name the name of the asker.
     */
    public void setAskerName(String name) {
        this.counter = 0;
        this.askerName = name;
    }

    /**
     * A new id is created using the askerName and counter.
     *
     * @return the new id.
     */
    public String createId() {
        String newId = this.askerName + this.counter;
        this.counter++;
        return newId;
    }

    /**
     * Returns the singleton instance of IdFactory.
     *
     * @return the instance of IdFactory.
     */
    public static IdFactory getInstance() {
        if (idFactory == null) idFactory = new IdFactory();
        return idFactory;
    }

    public int getCounter() {
        return counter;
    }
}