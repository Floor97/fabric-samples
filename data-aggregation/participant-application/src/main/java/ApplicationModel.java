import applications.operator.OperatorKeyStore;

import java.util.HashMap;
import java.util.HashSet;

public class ApplicationModel {

    private static ApplicationModel applicationModel = null;

    private final HashMap<String, OperatorKeyStore> operatorKeys;
    private final HashSet<String> ids;
    private int operatorThreshold = 0;

    private ApplicationModel() {
        operatorKeys = new HashMap<>();
        ids = new HashSet<>();
    }

    /**
     * Adds the id to the list of ids that the application is keeping track of. Also adds the
     * key used by the operator for this process.
     *
     * @param id   the unique id of the process.
     * @param keys the key the operator uses in this process.
     */
    public void addProcess(String id, OperatorKeyStore keys) {
        operatorKeys.put(id, keys);
        ids.add(id);
    }

    /**
     * Removes the id and key of the process from the respective lists.
     *
     * @param id the unique id of the process.
     * @return true if the process was in the ids list, false otherwise.
     */
    public boolean removeProcess(String id) {
        operatorKeys.remove(id);
        return ids.remove(id);
    }

    public boolean containsId(String id) {
        return this.ids.contains(id);
    }

    public static ApplicationModel getInstance() {
        if (applicationModel == null) applicationModel = new ApplicationModel();
        return applicationModel;
    }

    public OperatorKeyStore getKey(String id) {
        return operatorKeys.get(id);
    }

    public int getOperatorThreshold() {
        return operatorThreshold;
    }

    public void setOperatorThreshold(int operatorThreshold) {
        this.operatorThreshold = operatorThreshold;
    }
}
