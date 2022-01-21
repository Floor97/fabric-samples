import applications.operator.AggregationTransactions;
import applications.operator.DataAndNonces;
import applications.operator.DataQueryTransactions;
import applications.operator.ParticipantTransaction;
import datatypes.aggregationprocess.AggregationProcess;
import datatypes.dataquery.DataQuery;
import datatypes.values.Nonces;
import datatypes.values.Pair;
import org.bouncycastler.crypto.InvalidCipherTextException;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractEvent;
import org.hyperledger.fabric.gateway.ContractException;
import org.hyperledger.fabric.shim.ChaincodeException;

import java.io.IOException;
import java.math.BigInteger;
import java.util.NoSuchElementException;
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
        ApplicationController.setAggregationProcessConsumers(contractAgg, contractQuery);
        ApplicationController.setDataQueryConsumers(contractQuery, contractAgg);

        while (true) {
            System.out.println("Please select a transaction: exists, or change threshold: threshold. Type exit to stop.");
            try {
                switch (ParticipantTransaction.getNext()) {
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
            } catch(NoSuchElementException e) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException interruptedException) {
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
                        System.out.println("StartQuery");
                        int index = AggregationTransactions.start(contractAgg, data.getSettings().getNrExpectedParticipants(), data);
                        if (index == -1) return;
                        ApplicationModel.getInstance().addProcess(data.getId(), index);

                        ApplicationController.ruleTimeLimit(contractQuery, contractAgg, data);
                        break;
                    case "RemoveQuery":
                        System.out.println("RemoveQuery");
                        AggregationTransactions.remove(contractAgg, data.getId());
                        break;
                }
            } catch (ContractException | IOException | InterruptedException | TimeoutException e) {
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
    private static void setAggregationProcessConsumers(Contract contractAgg, Contract contractQuery) {
        Consumer<ContractEvent> consumer = contractEvent -> {
            try {
                if (!contractEvent.getTransactionEvent().isValid()) return;
                AggregationProcess aggregationProcess = AggregationProcess.deserialize(contractEvent.getPayload().get());
                switch (contractEvent.getName()) {
                    case "StartAggregating":
                        if (ApplicationModel.getInstance().getOperatorThreshold() > aggregationProcess.getNrOperatorsSelected()
                                || ApplicationModel.getInstance().getIndex(aggregationProcess.getId()) == null) return;

                        System.out.println("StartAggregation");

                        Pair<BigInteger, BigInteger[]> dataAndNonces = DataAndNonces.generateDataAndNonces(aggregationProcess.getNrOperatorsSelected());
                        AggregationTransactions.add(contractAgg, aggregationProcess.getId(), dataAndNonces.getP1(), dataAndNonces.getP2());
                        break;
                    case "ParticipantsReached":
                        synchronized (ApplicationModel.getInstance()) {
                            System.out.println("ParticipantsReached");
                            Integer index = ApplicationModel.getInstance().getIndex(aggregationProcess.getId());
                            if (index == null) return;

                            BigInteger reEncryptedNonce = Nonces.condenseNonces(Nonces.getOperatorNonces(
                                    index,
                                    aggregationProcess.getIpfsFile().getNonces()
                                    )
                            );

                            applications.operator.DataQueryTransactions.add(contractQuery, aggregationProcess.getId(), aggregationProcess.getIpfsFile(),
                                    reEncryptedNonce, index);
                            ApplicationModel.getInstance().removeProcess(aggregationProcess.getId());
                            break;
                        }
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
     */
    private static void ruleTimeLimit(Contract contractQuery, Contract contractAgg, DataQuery data) {
        TimerTask action = new TimerTask() {
            public void run() {
                synchronized (ApplicationModel.getInstance()) {
                    try {
                        Integer index = ApplicationModel.getInstance().getIndex(data.getId());
                        if (index == null) return;
                        AggregationProcess aggregationProcess = AggregationTransactions.close(contractAgg, data.getId());

                        BigInteger reEncryptedNonce = Nonces.condenseNonces(
                                Nonces.getOperatorNonces(
                                        index,
                                        aggregationProcess.getIpfsFile().getNonces()
                                )
                        );

                        DataQueryTransactions.add(contractQuery, aggregationProcess.getId(), aggregationProcess.getIpfsFile(), reEncryptedNonce, index);
                        ApplicationModel.getInstance().removeProcess(data.getId());

                    } catch (ChaincodeException e) {
                        System.err.println(e.getMessage());
                    } catch (InterruptedException | TimeoutException | IOException | ContractException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        new Timer().schedule(action, data.getSettings().getDuration() * 1000);

    }
}
