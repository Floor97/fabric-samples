import applications.asker.DataQueryTransactions;
import applications.asker.IdFactory;
import applications.operator.ParticipantTransaction;
import datatypes.dataquery.DataQuery;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractEvent;
import org.hyperledger.fabric.gateway.ContractException;
import org.hyperledger.fabric.shim.ChaincodeException;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class ApplicationController {

    /**
     * The main loop of the application is started. The user will be prompted with options and can
     * decide by entering a name which functionality to use. The existing functionalities are
     * exists, start, close, retrieve, remove and exit.
     *
     * @param contract the data query contract.
     */
    public static void applicationLoop(Contract contract) {
        ApplicationController.setContractListener(contract);
        Scanner scan = new Scanner(System.in);

        System.out.println("username: ");
        IdFactory.getInstance().setAskerName(scan.next());

        while (true) {
            System.out.println("Please select a transaction: exists, start, close, retrieve or remove. Type exit to stop.");
            try {
                switch (scan.next()) {
                    case "start":
                        String newId = DataQueryTransactions.start(contract);
                        ApplicationModel.getInstance().addProcess(newId);
                        System.out.println("End Step 1: " + System.currentTimeMillis());
                        break;
                    case "close":
                        DataQueryTransactions.close(contract);
                        break;
                    case "retrieve":
                        DataQueryTransactions.retrieve(contract);
                        break;
                    case "remove":
                        String id = ParticipantTransaction.scanNextLine("Transaction Remove selected\nID: ");
                        DataQueryTransactions.remove(contract, id);
                        ApplicationModel.getInstance().removeProcess(id);
                        break;
                    case "exists":
                        DataQueryTransactions.exists(contract);
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
            } catch (ContractException | InterruptedException | TimeoutException | IOException e) {
                System.err.println("Error occured in handling transaction!");
                e.printStackTrace();
            }
        }
    }

    /**
     * The listener for the data query contract is created and set.
     *
     * @param contract the data query contract.
     */
    private static void setContractListener(Contract contract) {
        Consumer<ContractEvent> consumer = contractEvent -> {
            if (!contractEvent.getTransactionEvent().isValid() || !"DoneQuery".equals(contractEvent.getName())) return;
            System.out.println("Begin Step 8: " + System.currentTimeMillis());

            try {
                DataQuery dataQuery = DataQuery.deserialize(contractEvent.getPayload().get());
                BigInteger dataAndNonces = dataQuery.getIpfsFile().getData();
                BigInteger[] nonces = dataQuery.getIpfsFile().getNonces();

                for (BigInteger nonce : nonces) {
                    dataAndNonces = dataAndNonces.subtract(nonce);
                }

                System.out.println("Result of " + dataQuery.getId() + " is " + dataAndNonces.toString());
                System.out.println("End Step 8: " + System.currentTimeMillis());

            } catch (IOException e) {
                System.err.println("Could not deserialize data query asset!");
                e.printStackTrace();
            }
        };
        contract.addContractListener(consumer);
    }
}
