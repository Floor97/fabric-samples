import applications.operator.AggregationTransactions;
import applications.operator.DataGenerator;
import applications.operator.OperatorKeyStore;
import applications.operator.QueryTransactions;
import datatypes.aggregationprocess.AggregationProcess;
import datatypes.dataquery.DataQuery;
import datatypes.values.EncryptedData;
import datatypes.values.EncryptedNonces;
import datatypes.values.Pair;
import encryption.NTRUEncryption;
import org.bouncycastler.crypto.InvalidCipherTextException;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractEvent;
import org.hyperledger.fabric.gateway.ContractException;

import java.io.IOException;
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
            } catch (ContractException | IOException e) {
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
            if(!contractEvent.getTransactionEvent().isValid()) return;
            System.out.println("Event occured: " + contractEvent.getName());
            try {
                DataQuery data = DataQuery.deserialize(contractEvent.getPayload().get());
                switch (contractEvent.getName()) {
                    case "StartQuery":
                        System.out.println("StartQuery");
                        OperatorKeyStore keystore = AggregationTransactions.start(contractAgg, data);
                        if(keystore.getIndex() != -1) ApplicationModel.getInstance().addProcess(data.getId(), keystore);
                        if (keystore.getIndex() != 0) return;

                        ApplicationController.eventTimeLimit(contractQuery, contractAgg, data, keystore);
                        break;
                    case "ResultQuery":
                        System.out.println("ResultQuery");
                        OperatorKeyStore opKeystore = ApplicationModel.getInstance().getKey(data.getId());
                        if (opKeystore == null || opKeystore.getIndex() == 0) return;

                        AggregationProcess aggregationProcess = AggregationTransactions.retrieve(contractAgg, data.getId());
                        QueryTransactions.addOperator(contractQuery, "AddOperatorN", data.getId(), aggregationProcess.getIpfsFile(),
                                EncryptedNonces.condenseNonces(
                                        opKeystore,
                                        EncryptedNonces.getOperatorNonces(aggregationProcess, opKeystore.getIndex()),
                                        NTRUEncryption.serialize(data.getIpfsFile().getPostqKey())
                                ),
                                opKeystore.getIndex()
                        );
                    case "RemoveQuery":
                        System.out.println("RemoveQuery");
                        if (ApplicationModel.getInstance().removeProcess(data.getId()))
                            AggregationTransactions.remove(contractAgg, data.getId());
                        break;
                }
            } catch (ContractException e) {
                System.out.println(e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
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
                if(!contractEvent.getTransactionEvent().isValid()) return;
                AggregationProcess aggregationProcess = AggregationProcess.deserialize(contractEvent.getPayload().get());
                System.out.println("Event: " + contractEvent.getName());
                if ("StartAggregating".equals(contractEvent.getName())
                        && (ApplicationModel.getInstance().getOperatorThreshold() <= aggregationProcess.getIpfsFile().getOperatorKeys().length
                        || ApplicationModel.getInstance().getKey(aggregationProcess.getId()) != null)) {
                    System.out.println("StartAggregation");
                    Pair<EncryptedData, EncryptedNonces> dataAndNonces = DataGenerator.generateDataAndNonces(
                            aggregationProcess.getIpfsFile().getPaillierKey(),
                            Arrays.stream(aggregationProcess.getIpfsFile().getOperatorKeys()).map(NTRUEncryption::serialize).toArray(String[]::new)
                    );
                    AggregationTransactions.add(contractAgg, aggregationProcess.getId(), dataAndNonces.getP1(), dataAndNonces.getP2());
                }
            } catch (InterruptedException | TimeoutException | InvalidCipherTextException | IOException e) {
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
     * @param keystore      the operator keystore.
     */
    private static void eventTimeLimit(Contract contractQuery, Contract contractAgg, DataQuery data, OperatorKeyStore keystore) {
        TimerTask action = new TimerTask() {
            public void run() {
                try {
                    AggregationProcess aggregationProcess = AggregationTransactions.close(contractAgg, data.getId());

                    QueryTransactions.addOperator(contractQuery, "AddOperatorZero", aggregationProcess.getId(), aggregationProcess.getIpfsFile(),
                            EncryptedNonces.condenseNonces(
                                    keystore,
                                    EncryptedNonces.getOperatorNonces(aggregationProcess, keystore.getIndex()),
                                    NTRUEncryption.serialize(aggregationProcess.getIpfsFile().getPostqKey())
                            ), keystore.getIndex()
                    );

                } catch (InterruptedException | TimeoutException | InvalidCipherTextException | IOException e) {
                    e.printStackTrace();
                } catch (ContractException e) {
                    System.out.println(e.getMessage());
                }
            }
        };
        new Timer().schedule(action, data.getSettings().getDuration() * 1000);
    }
}
