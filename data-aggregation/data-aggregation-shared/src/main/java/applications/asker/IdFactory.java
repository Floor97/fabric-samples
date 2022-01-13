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

    public void setAskerName(String name) {
        this.counter = 0;
        this.askerName = name;
    }

    public static IdFactory getInstance() {
        if(idFactory == null) idFactory = new IdFactory();
        return idFactory;
    }

    public String createId() {
        String newId = this.askerName + this.counter;
        this.counter++;
        return newId;
    }
}