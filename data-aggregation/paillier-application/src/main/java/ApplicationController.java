import applications.IdFactory;
import applications.KeyStore;
import applications.operator.AggregationTransactions;
import applications.operator.OperatorKeyStore;
import applications.operator.QueryTransactions;
import applications.operator.generators.DataGenerator;
import datatypes.aggregationprocess.AggregationProcess;
import datatypes.dataquery.DataQuery;
import datatypes.values.EncryptedData;
import datatypes.values.EncryptedNonces;
import org.bouncycastler.crypto.InvalidCipherTextException;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractEvent;
import org.hyperledger.fabric.gateway.ContractException;
import shared.Pair;

import java.util.Arrays;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class ApplicationController {

    /**
     * The main loop of the application is started. The user will be prompted with options and can
     * decide by entering a name which functionality to use. The existing functionalities are
     * exists and exit.
     *
     * @param contractAgg   the aggregation process contract.
     * @param contractQuery the data query contract.
     */
    public static void applicationLoop(Contract contractAgg, Contract contractQuery) {
        ApplicationController.setDataAggregationContractListener(contractAgg);
        ApplicationController.setQueryContractListener(contractQuery, contractAgg);
        Scanner scan = new Scanner(System.in);

        System.out.println("username: ");
        IdFactory.getInstance().setAskerName(scan.next());

        while (true) {
            System.out.println("Please select a transaction: exists. Type exit to stop.");
            try {
                switch (scan.next()) {
                    case "exists":
                        AggregationTransactions.exists(contractAgg);
                        break;
                    case "exit":
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Unrecognised transaction");
                        break;
                }
            } catch (ContractException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * The listeners for the data query contract are set. This involves the events StartQuery, ResultQuery,
     * and RemoveQuery.
     *
     * @param contractQuery the data query contract.
     * @param contractAgg   the aggregation process contract.
     */
    private static void setQueryContractListener(Contract contractQuery, Contract contractAgg) {
        Consumer<ContractEvent> consumer = contractEvent -> {
            DataQuery data = DataQuery.deserialize(contractEvent.getPayload().get());
            try {
                switch (contractEvent.getName()) {
                    case "StartQuery":
                        OperatorKeyStore keystore = AggregationTransactions.start(contractAgg, data);
                        ApplicationModel.getInstance().addProcess(data.getId(), keystore);
                        if (keystore.getIndex() != 0) return;

                        ApplicationController.eventTimeLimit(contractQuery, contractAgg, data, keystore);
                        break;
                    case "ResultQuery":
                        OperatorKeyStore opKeystore = ApplicationModel.getInstance().getKey(data.getId());
                        if (opKeystore == null) return;

                        AggregationProcess aggregationProcess = AggregationProcess.deserialize(contractEvent.getPayload().get());
                        QueryTransactions.add(contractQuery, data.getId(), aggregationProcess.getIpfsFile(),
                                EncryptedNonces.condenseNonces(
                                        opKeystore.getPostQuantumKeys(),
                                        EncryptedNonces.getOperatorNonces(aggregationProcess, opKeystore.getIndex()),
                                        KeyStore.pqPubKeyToString(data.getIpfsFile().getPostqKey())
                                ),
                                opKeystore.getIndex()
                        );
                    case "RemoveQuery":
                        if (ApplicationModel.getInstance().removeProcess(data.getId()))
                            AggregationTransactions.remove(contractAgg, data.getId());
                        break;
                }
            } catch (InterruptedException | TimeoutException | InvalidCipherTextException e) {
                e.printStackTrace();
            } catch (ContractException e) {
                System.out.println(e.getMessage());
            }
        };
        contractQuery.addContractListener(consumer);
    }

    /**
     * The listeners for the aggregation process contract are set. This involves the StartAggregation event.
     *
     * @param contractAgg the aggregation process contract.
     */
    private static void setDataAggregationContractListener(Contract contractAgg) {
        Consumer<ContractEvent> consumer = contractEvent -> {
            try {
                AggregationProcess aggregationProcess = AggregationProcess.deserialize(contractEvent.getPayload().get());
                if ("StartAggregation".equals(contractEvent.getName())
                        && (ApplicationModel.getInstance().getOperatorThreshold() <= aggregationProcess.getIpfsFile().getOperatorKeys().length
                        || ApplicationModel.getInstance().getKey(aggregationProcess.getId()) != null)) {

                    Pair<EncryptedData, EncryptedNonces> dataAndNonces = DataGenerator.generateDataAndNonces(
                            aggregationProcess.getIpfsFile().getPaillierKey(),
                            Arrays.stream(aggregationProcess.getIpfsFile().getOperatorKeys()).map(KeyStore::pqPubKeyToString).toArray(String[]::new)
                    );
                    AggregationTransactions.add(contractAgg, aggregationProcess.getId(), dataAndNonces.getP1(), dataAndNonces.getP2());
                }
            } catch (InterruptedException | TimeoutException | org.bouncycastler.crypto.InvalidCipherTextException e) {
                e.printStackTrace();
            } catch (ContractException e) {
                System.out.println(e.getMessage());
            }
        };

        contractAgg.addContractListener(consumer);
    }

    /**
     * The actions after the time limit of the data query process is reached are set.
     *
     * @param contractQuery the data query contract.
     * @param contractAgg   the aggregation process contract.
     * @param data          the data
     */
    private static void eventTimeLimit(Contract contractQuery, Contract contractAgg, DataQuery data, OperatorKeyStore keystore) {
        TimerTask action = new TimerTask() {
            public void run() {
                try {
                    AggregationProcess aggregationProcess = AggregationTransactions.close(contractAgg, data.getId());

                    QueryTransactions.add(contractQuery, aggregationProcess.getId(), aggregationProcess.getIpfsFile(),
                            EncryptedNonces.condenseNonces(
                                    keystore.getPostQuantumKeys(),
                                    EncryptedNonces.getOperatorNonces(aggregationProcess, keystore.getIndex()),
                                    KeyStore.pqPubKeyToString(aggregationProcess.getIpfsFile().getPostqKey())
                            ), keystore.getIndex()
                    );

                } catch (InterruptedException | TimeoutException | InvalidCipherTextException e) {
                    e.printStackTrace();
                } catch (ContractException e) {
                    System.out.println(e.getMessage());
                }
            }
        };
        new Timer().schedule(action, data.getSettings().getDuration() * 1000);
    }
}
