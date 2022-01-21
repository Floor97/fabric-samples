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

        System.out.println("username: ");
        IdFactory.getInstance().setAskerName(ParticipantTransaction.getNext());
        System.out.println("Operators: ");
        String operators = ParticipantTransaction.getNext();
        System.out.println("Participants: ");
        String nrExpParticipants = ParticipantTransaction.getNext();

        while (IdFactory.getInstance().getCounter() < 100) {
            try {
                System.out.println("Start Cycle " + IdFactory.getInstance().getCounter() + ": " + System.currentTimeMillis());
                String id = DataQueryTransactions.start(contract, operators, nrExpParticipants);
                ApplicationModel.getInstance().addProcess(id);
                Thread.sleep(10000);
            } catch (ContractException | InterruptedException | TimeoutException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        System.out.println("Done! Press any to quit...");
        ParticipantTransaction.getNext();
    }

    /**
     * The listener for the data query contract is created and set.
     *
     * @param contract the data query contract.
     */
    private static void setContractListener(Contract contract) {
        Consumer<ContractEvent> consumer = contractEvent -> {
            try {
                if (!contractEvent.getTransactionEvent().isValid() || !"DoneQuery".equals(contractEvent.getName())) return;
                System.out.println("DoneQuery");
                DataQuery dataQuery = DataQuery.deserialize(contractEvent.getPayload().get());
                BigInteger dataAndNonces = dataQuery.getIpfsFile().getData();
                BigInteger[] nonces = dataQuery.getIpfsFile().getNonces();



                for (BigInteger nonce : nonces) {
                    dataAndNonces = dataAndNonces.subtract(nonce);
                }

                System.out.println("Result of " + dataQuery.getId() + " is " + dataAndNonces.toString());
                System.out.println("End Cycle "  + dataQuery.getId() + ": " + System.currentTimeMillis());
            } catch (Exception e) {
                System.err.println("Could not deserialize data query asset!");
                e.printStackTrace();
            }
        };
        contract.addContractListener(consumer);
    }
}
