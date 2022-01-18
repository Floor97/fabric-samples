
import java.util.HashSet;

public class ApplicationModel {

    private static ApplicationModel applicationModel = null;

    private final HashSet<String> ids;

    private ApplicationModel() {
        ids = new HashSet<>();
    }

    /**
     * Adds the id to the list of ids that the application is keeping track of.
     *
     * @param id the unique id of the process.
     */
    public void addProcess(String id) {
        ids.add(id);
    }

    /**
     * Removes the id from the respective list.
     *
     * @param id the unique id of the process.
     * @return true if the process was in the ids list, false otherwise.
     */
    public boolean removeProcess(String id) {
        return ids.remove(id);
    }

    public static ApplicationModel getInstance() {
        if (applicationModel == null) applicationModel = new ApplicationModel();
        return applicationModel;
    }

    public HashSet<String> getIds() {
        return ids;
    }

    public boolean containsId(String id) {
        return ids.contains(id);
    }
}
