import java.util.HashMap;
import java.util.HashSet;

public class ApplicationModel {

    private static ApplicationModel applicationModel = null;

    private final HashMap<String, Integer> operatorIndex;
    private final HashSet<String> ids;
    private int operatorThreshold = 0;

    private ApplicationModel() {
        operatorIndex = new HashMap<>();
        ids = new HashSet<>();
    }

    /**
     * Adds the id to the list of ids that the application is keeping track of. Also adds the
     * key used by the operator for this process.
     *
     * @param id    the unique id of the process.
     * @param index the index the operator uses in this process.
     */
    public void addProcess(String id, int index) {
        operatorIndex.put(id, index);
        ids.add(id);
    }

    /**
     * Removes the id and key of the process from the respective lists.
     *
     * @param id the unique id of the process.
     * @return true if the process was in the ids list, false otherwise.
     */
    public boolean removeProcess(String id) {
        operatorIndex.remove(id);
        return ids.remove(id);
    }

    public boolean containsId(String id) {
        return this.ids.contains(id);
    }

    public static ApplicationModel getInstance() {
        if (applicationModel == null) applicationModel = new ApplicationModel();
        return applicationModel;
    }

    public Integer getIndex(String id) {
        return operatorIndex.get(id);
    }

    public int getOperatorThreshold() {
        return operatorThreshold;
    }

    public void setOperatorThreshold(int operatorThreshold) {
        this.operatorThreshold = operatorThreshold;
    }
}
