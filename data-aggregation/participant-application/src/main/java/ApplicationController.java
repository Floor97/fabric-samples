import applications.operator.*;
import datatypes.aggregationprocess.AggregationProcess;
import datatypes.dataquery.DataQuery;
import datatypes.values.EncryptedData;
import datatypes.values.EncryptedNonce;
import datatypes.values.EncryptedNonces;
import datatypes.values.Pair;
import encryption.NTRUEncryption;
import org.bouncycastler.crypto.InvalidCipherTextException;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractEvent;
import org.hyperledger.fabric.gateway.ContractException;
import org.hyperledger.fabric.shim.ChaincodeException;

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
        ApplicationController.setAggregationProcessConsumers(contractAgg);
        ApplicationController.setDataQueryConsumers(contractQuery, contractAgg);
        Scanner scan = new Scanner(System.in);

        while (true) {
            System.out.println("Please select a transaction: exists, or change threshold: threshold. Type exit to stop.");
            try {
                switch (scan.next()) {
                    case "exists":
                        AggregationTransactions.exists(contractAgg);
                        break;
                    case "threshold":
                        ApplicationModel.getInstance().setOperatorThreshold(
                                Integer.parseInt(ParticipantTransaction.scanNextLine("New threshold: ")));
                        break;
                    case "exit":
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Unrecognised transaction");
                        break;
                }
            } catch (ChaincodeException e) {
                System.err.println(e.getMessage());
            } catch (ContractException | IOException e) {
                e.printStackTrace();
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
    private static void setDataQueryConsumers(Contract contractQuery, Contract contractAgg) {
        Consumer<ContractEvent> consumer = contractEvent -> {
            if (!contractEvent.getTransactionEvent().isValid()) return;
            try {
                DataQuery data = DataQuery.deserialize(contractEvent.getPayload().get());
                switch (contractEvent.getName()) {
                    case "StartQuery":
                        System.out.println("Begin Step 2:" + System.currentTimeMillis());
                        System.out.println("StartQuery");
                        OperatorKeyStore keystore = AggregationTransactions.start(contractAgg, data);
                        if (keystore.getIndex() != -1)
                            ApplicationModel.getInstance().addProcess(data.getId(), keystore);
                        if (keystore.getIndex() != 0) {
                            System.out.println("End Step 2: " + System.currentTimeMillis());
                            return;
                        }

                        ApplicationController.ruleTimeLimit(contractQuery, contractAgg, data, keystore);
                        System.out.println("End Step 2: " + System.currentTimeMillis());
                        break;
                    case "ResultQuery":
                        System.out.println("ResultQuery");
                        OperatorKeyStore opKeystore = ApplicationModel.getInstance().getKey(data.getId());
                        if (opKeystore == null || opKeystore.getIndex() == 0) return;

                        System.out.println("Begin Step 5: " + System.currentTimeMillis());
                        AggregationProcess aggregationProcess = AggregationTransactions.retrieve(contractAgg, data.getId());
                        System.out.println("End Step 5: " + System.currentTimeMillis());
                        System.out.println("Begin Step 6: " + System.currentTimeMillis());
                        EncryptedNonce reEncryptedNonce = EncryptedNonces.condenseNonces(
                                opKeystore,
                                EncryptedNonces.getOperatorNonces(aggregationProcess, opKeystore.getIndex()),
                                NTRUEncryption.serialize(data.getIpfsFile().getPostqKey())
                        );
                        System.out.println("End Step 6: " + System.currentTimeMillis());
                        System.out.println("Begin Step 7: " + System.currentTimeMillis());
                        DataQueryTransactions.addOperator(contractQuery, "AddOperatorN", data.getId(), aggregationProcess.getIpfsFile(),
                                reEncryptedNonce,
                                opKeystore.getIndex()
                        );
                        System.out.println("End Step 7: " + System.currentTimeMillis());
                        break;
                    case "RemoveQuery":
                        System.out.println("RemoveQuery");
                        if (ApplicationModel.getInstance().removeProcess(data.getId()))
                            AggregationTransactions.remove(contractAgg, data.getId());
                        break;
                }
            } catch (ChaincodeException e) {
                System.err.println(e.getMessage());
            } catch (InvalidCipherTextException | ContractException | IOException | InterruptedException | TimeoutException e) {
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
    private static void setAggregationProcessConsumers(Contract contractAgg) {
        Consumer<ContractEvent> consumer = contractEvent -> {
            try {
                if (!contractEvent.getTransactionEvent().isValid()) return;
                AggregationProcess aggregationProcess = AggregationProcess.deserialize(contractEvent.getPayload().get());
                if (!"StartAggregating".equals(contractEvent.getName())) return;
                System.out.println("Begin Step 3: " + System.currentTimeMillis());
                if (ApplicationModel.getInstance().getOperatorThreshold() <= aggregationProcess.getIpfsFile().getOperatorKeys().length
                        || ApplicationModel.getInstance().getKey(aggregationProcess.getId()) != null) {
                    System.out.println("End Step 3: " + System.currentTimeMillis());
                    System.out.println("Start Step 4: " + System.currentTimeMillis());
                    System.out.println("StartAggregation");

                    Pair<EncryptedData, EncryptedNonces> dataAndNonces = DataAndNonces.generateDataAndNonces(
                            aggregationProcess.getIpfsFile().getPaillierKey(),
                            Arrays.stream(aggregationProcess.getIpfsFile().getOperatorKeys()).map(NTRUEncryption::serialize).toArray(String[]::new)
                    );
                    AggregationTransactions.add(contractAgg, aggregationProcess.getId(), dataAndNonces.getP1(), dataAndNonces.getP2());
                    System.out.println("End Step 4: " + System.currentTimeMillis());
                }
            } catch (ChaincodeException e) {
                System.err.println(e.getMessage());
            } catch (InterruptedException | TimeoutException | InvalidCipherTextException | IOException | ContractException e) {
                e.printStackTrace();
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
    private static void ruleTimeLimit(Contract contractQuery, Contract contractAgg, DataQuery data, OperatorKeyStore keystore) {
        TimerTask action = new TimerTask() {
            public void run() {
                try {
                    System.out.println("Begin Step 5: " + System.currentTimeMillis());
                    AggregationProcess aggregationProcess = AggregationTransactions.close(contractAgg, data.getId());
                    System.out.println("End Step 5: " + System.currentTimeMillis());

                    System.out.println("Begin Step 6: " + System.currentTimeMillis());
                    EncryptedNonce reEncryptedNonce = EncryptedNonces.condenseNonces(
                            keystore,
                            EncryptedNonces.getOperatorNonces(aggregationProcess, keystore.getIndex()),
                            NTRUEncryption.serialize(aggregationProcess.getIpfsFile().getPostqKey())
                    );
                    System.out.println("End Step 6: " + System.currentTimeMillis());
                    System.out.println("Begin Step 7: " + System.currentTimeMillis());
                    DataQueryTransactions.addOperator(contractQuery, "AddOperatorZero", aggregationProcess.getId(), aggregationProcess.getIpfsFile(),
                            reEncryptedNonce, keystore.getIndex()
                    );
                    System.out.println("End Step 7: " + System.currentTimeMillis());

                } catch (ChaincodeException e) {
                    System.err.println(e.getMessage());
                } catch (InterruptedException | TimeoutException | InvalidCipherTextException | IOException | ContractException e) {
                    e.printStackTrace();
                }
            }
        };
        new Timer().schedule(action, data.getSettings().getDuration() * 1000);
    }
}
