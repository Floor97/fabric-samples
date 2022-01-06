import java.util.HashMap;
import java.util.HashSet;

public class ApplicationModel {

    private static ApplicationModel applicationModel = null;

    private final HashMap<String, TempKeystore> queryKeys;
    private final HashSet<String> ids;

    public static final String CC_NAME = "query";
    public static final String CONTRACT_NAME = "query.eventcontract";
    public static final String CHANNEL_NAME = "mychannel";

    private ApplicationModel() {
        queryKeys = new HashMap<>();
        ids = new HashSet<>();
    }

    public TempKeystore getKey(String id) {
        return queryKeys.get(id);
    }

    public void addKey(String id, TempKeystore keys) {
        queryKeys.put(id, keys);
    }

    public HashSet<String> getIds() {
        return ids;
    }

    public boolean containsId(String id) {
        return ids.contains(id);
    }

    public boolean removeProcess(String id) {
        queryKeys.remove(id);
        return ids.remove(id);
    }

    public void addProcess(String id, TempKeystore keys) {
        this.addKey(id, keys);
        ids.add(id);
    }

    public static ApplicationModel getInstance() {
        if(applicationModel == null) applicationModel = new ApplicationModel();
        return applicationModel;
    }
}
