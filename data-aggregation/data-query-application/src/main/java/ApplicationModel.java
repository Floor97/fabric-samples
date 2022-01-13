import applications.DataQueryKeyStore;

import java.util.HashMap;
import java.util.HashSet;

public class ApplicationModel {

    private static ApplicationModel applicationModel = null;

    private final HashMap<String, DataQueryKeyStore> queryKeys;
    private final HashSet<String> ids;

    private ApplicationModel() {
        queryKeys = new HashMap<>();
        ids = new HashSet<>();
    }

    /**
     * Adds the id to the list of ids that the application is keeping track of. Also adds the
     * key used by the operator for this process.
     *
     * @param id   the unique id of the process.
     * @param keys the key the operator uses in this process.
     */
    public void addProcess(String id, DataQueryKeyStore keys) {
        this.addKey(id, keys);
        ids.add(id);
    }

    /**
     * Removes the id and key of the process from the respective lists.
     *
     * @param id the unique id of the process.
     * @return true if the process was in the ids list, false otherwise.
     */
    public boolean removeProcess(String id) {
        queryKeys.remove(id);
        return ids.remove(id);
    }

    public static ApplicationModel getInstance() {
        if (applicationModel == null) applicationModel = new ApplicationModel();
        return applicationModel;
    }

    public DataQueryKeyStore getKey(String id) {
        return queryKeys.get(id);
    }

    public void addKey(String id, DataQueryKeyStore keys) {
        queryKeys.put(id, keys);
    }

    public HashSet<String> getIds() {
        return ids;
    }

    public boolean containsId(String id) {
        return ids.contains(id);
    }
}
