import applications.operator.OperatorKeyStore;

import java.util.HashMap;
import java.util.HashSet;

public class ApplicationModel {

    private static ApplicationModel applicationModel = null;

    private final HashMap<String, OperatorKeyStore> queryKeys;
    private final HashSet<String> ids;

    public static final String CC_NAME_AGG = "aggregationprocess";
    public static final String CONTRACT_NAME_AGG = "aggregationprocess.pailliercontract";
    public static final String CHANNEL_NAME_AGG = "mychannel";

    public static final String CC_NAME_QUERY = "query";
    public static final String CONTRACT_NAME_QUERY = "query.eventcontract";
    public static final String CHANNEL_NAME_QUERY = "mychannel";

    private int operatorThreshold = 10;

    private ApplicationModel() {
        queryKeys = new HashMap<>();
        ids = new HashSet<>();
    }

    public OperatorKeyStore getKey(String id) {
        return queryKeys.get(id);
    }

    public void addKey(String id, OperatorKeyStore keys) {
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

    public void addProcess(String id, OperatorKeyStore keys) {
        this.addKey(id, keys);
        ids.add(id);
    }

    public static ApplicationModel getInstance() {
        if(applicationModel == null) applicationModel = new ApplicationModel();
        return applicationModel;
    }

    public int getOperatorThreshold() {
        return operatorThreshold;
    }

    public void setOperatorThreshold(int operatorThreshold) {
        this.operatorThreshold = operatorThreshold;
    }
}
