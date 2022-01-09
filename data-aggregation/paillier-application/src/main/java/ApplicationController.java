import applications.IdFactory;
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
            System.out.println("Please select a transaction: exists, start, add, close, retrieve or remove");
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
            } catch (ContractException | InterruptedException | TimeoutException e) {
                e.printStackTrace();
            }

        }
    }

    private static void setQueryContractListener(Contract contractQuery, Contract contractAgg) {
        Consumer<ContractEvent> consumer = contractEvent -> {
            DataQuery data = DataQuery.deserialize(contractEvent.getPayload().get());
            try {
                switch (contractEvent.getName()) {
                    case "StartQuery":
                        System.out.println("StartQuery Event");
                        ApplicationController.startQuery(contractQuery, contractAgg, data);
                        break;
                    case "ResultQuery":
                        System.out.println("ResultQuery Event");
                        OperatorKeyStore opKeystore = ApplicationModel.getInstance().getKey(data.getId());
                        if (opKeystore != null) {
                            AggregationProcess aggregationProcess = AggregationTransactions.retrieve(contractAgg, data.getId());
                            QueryTransactions.add(contractQuery, data.getId(), aggregationProcess.getData(),
                                    EncryptedNonces.condenseNonces(
                                            opKeystore.getPostQuantumKeys(),
                                            EncryptedNonces.getOperatorNonces(aggregationProcess, opKeystore.getIndex()),
                                            data.getSettings().getPostQuantumPk()
                                    )
                            );
                        }
                    case "RemoveQuery":
                        System.out.println("RemoveQuery Event");
                        ApplicationModel.getInstance().removeProcess(data.getId());
                        AggregationTransactions.remove(contractAgg, data.getId());
                        break;
                    default:
                        System.out.println("Event occurred: " + contractEvent.getName());
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
                switch (contractEvent.getName()) {
                    case "StartSelection":
                        System.out.println("StartSelection Event");
                        OperatorKeyStore opKeystore = AggregationTransactions.addop(contractAgg, aggregationProcess.getId());
                        ApplicationModel.getInstance().addProcess(aggregationProcess.getId(), opKeystore);
                        break;
                    case "StartAggregation":
                        if(ApplicationModel.getInstance().getOperatorThreshold() > aggregationProcess.getData().getNrOperators() //if less operators than threshold
                                && ApplicationModel.getInstance().getKey(aggregationProcess.getId()) == null)                    //and self not operator, don't participate
                            return;
                        System.out.println("StartAggregation Event");
                        Pair<EncryptedData, EncryptedNonces> dataAndNonces = DataGenerator.generateDataAndNonces(aggregationProcess.getKeystore().getPaillierModulus(), aggregationProcess.getKeystore().getOperatorKeys());
                        AggregationTransactions.adddata(contractAgg, aggregationProcess.getId(), dataAndNonces.getP1().getData(), dataAndNonces.getP1().getExponent(), dataAndNonces.getP2());
                        break;
                    default:
                        System.out.println("Event occurred: " + contractEvent.getName());
                        break;
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
        OperatorKeyStore keystore = AggregationTransactions.start(contractAgg, contractQuery, data);
        ApplicationModel.getInstance().addProcess(data.getId(), keystore);

        Timer timer = new Timer();
        TimerTask action = new TimerTask() {
            public void run() {
                try {
                    AggregationProcess aggregationProcess = AggregationTransactions.close(contractAgg, data.getId());

                    QueryTransactions.add(contractQuery, aggregationProcess.getId(), aggregationProcess.getData(),
                            EncryptedNonces.condenseNonces(
                                    keystore.getPostQuantumKeys(),
                                    EncryptedNonces.getOperatorNonces(aggregationProcess, keystore.getIndex()),
                                    data.getSettings().getPostQuantumPk()
                            ));
                } catch (InterruptedException | TimeoutException | InvalidCipherTextException e) {
                    e.printStackTrace();
                } catch (ContractException e) {
                    System.out.println(e.getMessage());
                }
            }

        };
        timer.schedule(action, data.getSettings().getEndTime() * 1000);
    }
}
