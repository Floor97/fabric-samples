import applications.IdFactory;
import applications.operator.AggregationTransactions;
import applications.operator.generators.DataGenerator;
import datatypes.aggregationprocess.AggregationProcess;
import datatypes.dataquery.DataQuery;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractEvent;
import org.hyperledger.fabric.gateway.ContractException;
import org.hyperledger.fabric.shim.ChaincodeException;
import shared.Pair;

import javax.xml.crypto.Data;
import java.util.Scanner;
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
            //try {
                switch (scan.next()) {
                    case "exists":
                        //AggregationTransactions.exists(contractAgg);
                        break;
                    //case "start": DataAggregationTransactions.start(contractAgg); break;
                    //case "addop": DataAggregationTransactions.addop(contractAgg); break;
                    //case "adddata": DataAggregationTransactions.adddata(contractAgg); break;
                    //case "close": DataAggregationTransactions.close(contractAgg); break;
                    //case "retrieve": DataAggregationTransactions.retrieve(contractAgg); break;
                    //case "remove": DataAggregationTransactions.remove(contractAgg); break;
                    case "exit":
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Unrecognised transaction");
                        break;
                }
            //} catch (ContractException | InterruptedException | TimeoutException e) {
            //    e.printStackTrace();
            //}

        }
    }

    private static void setQueryContractListener(Contract contractQuery, Contract contractAgg) {
        Consumer<ContractEvent> consumer = contractEvent -> {
            DataQuery data = DataQuery.deserialize(contractEvent.getPayload().get());
            try {
                switch (contractEvent.getName()) {
                    case "StartQuery":
                        AggregationTransactions.start(contractAgg, contractQuery, data);
                        break;
                    case "RemoveQuery":
                        AggregationTransactions.remove(contractAgg, data.getId());
                        break;
                    default:
                        System.out.println("Event occurred: " + contractEvent.getName()); break;
                }
            } catch (ContractException | InterruptedException | TimeoutException e) {
                e.printStackTrace();
            } catch (ChaincodeException e) {
                System.out.println(e);
            }
        };
        contractQuery.addContractListener(consumer);
    }

    private static void setDataAggregationContractListener(Contract contractAgg) {
        Consumer<ContractEvent> consumer = contractEvent -> {
            AggregationProcess aggregationProcess = AggregationProcess.deserialize(contractEvent.getPayload().get());
            try{
                switch (contractEvent.getName()) {
                    case "StartSelection": AggregationTransactions.addop(contractAgg, aggregationProcess.getId()); break;
                    case "StartAggregation":
                        Pair<Pair<String, Integer>, byte[][]> dataAndNonces = DataGenerator.generateDataAndNonces(aggregationProcess.getKeystore().getPaillierModulus(), aggregationProcess.getKeystore().getOperatorKeys());
                        AggregationTransactions.adddata(contractAgg, aggregationProcess.getId(), dataAndNonces.getP1().getP1(), dataAndNonces.getP1().getP2(), dataAndNonces.getP2()); break; //todo add data and nonces
                    default:
                        System.out.println("Event occurred: " + contractEvent.getName()); break;
                }
            } catch (ContractException | InterruptedException | TimeoutException | org.bouncycastler.crypto.InvalidCipherTextException e) {
                e.printStackTrace();
            } catch (ChaincodeException e) {
                System.out.println(e);
            }
        };

        contractAgg.addContractListener(consumer);
    }
}
