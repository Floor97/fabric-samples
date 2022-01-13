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

    private static void setQueryContractListener(Contract contractQuery, Contract contractAgg) {
        Consumer<ContractEvent> consumer = contractEvent -> {
            DataQuery data = DataQuery.deserialize(contractEvent.getPayload().get());
            try {
                switch (contractEvent.getName()) {
                    case "StartQuery":
                        ApplicationController.startQuery(contractQuery, contractAgg, data);
                        break;
                    case "ResultQuery":
                        OperatorKeyStore opKeystore = ApplicationModel.getInstance().getKey(data.getId());
                        if (opKeystore == null) return;

                        AggregationProcess aggregationProcess = AggregationProcess.deserialize(contractEvent.getPayload().get());
                        QueryTransactions.add(contractQuery, data.getId(), aggregationProcess.getIpfsFile().getData(),
                                EncryptedNonces.condenseNonces(
                                        opKeystore.getPostQuantumKeys(),
                                        EncryptedNonces.getOperatorNonces(aggregationProcess, opKeystore.getIndex()),
                                        KeyStore.pqPubKeyToString(data.getIpfsFile().getPostqKey())
                                ),
                                aggregationProcess.getIpfsFile().getNonces().length,
                                opKeystore.getIndex()
                        );
                    case "RemoveQuery":
                        ApplicationModel.getInstance().removeProcess(data.getId());
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

    private static void setDataAggregationContractListener(Contract contractAgg) {
        Consumer<ContractEvent> consumer = contractEvent -> {
            AggregationProcess aggregationProcess = AggregationProcess.deserialize(contractEvent.getPayload().get());
            try {

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

    private static void startQuery(Contract contractQuery, Contract contractAgg, DataQuery data) throws ContractException, InterruptedException, TimeoutException {
        OperatorKeyStore keystore = AggregationTransactions.start(contractAgg, data);
        ApplicationModel.getInstance().addProcess(data.getId(), keystore);

        if (keystore.getIndex() != 0) return;

        Timer timer = new Timer();
        TimerTask action = new TimerTask() {
            public void run() {
                try {
                    AggregationProcess aggregationProcess = AggregationTransactions.close(contractAgg, data.getId());

                    QueryTransactions.add(contractQuery, aggregationProcess.getId(), aggregationProcess.getIpfsFile().getData(),
                            EncryptedNonces.condenseNonces(
                                    keystore.getPostQuantumKeys(),
                                    EncryptedNonces.getOperatorNonces(aggregationProcess, keystore.getIndex()),
                                    KeyStore.pqPubKeyToString(data.getIpfsFile().getPostqKey())
                            ),
                            aggregationProcess.getIpfsFile().getNonces().length,
                            keystore.getIndex()
                    );
                } catch (InterruptedException | TimeoutException | InvalidCipherTextException e) {
                    e.printStackTrace();
                } catch (ContractException e) {
                    System.out.println(e.getMessage());
                }
            }
        };
        timer.schedule(action, data.getSettings().getDuration() * 1000);
    }
}
