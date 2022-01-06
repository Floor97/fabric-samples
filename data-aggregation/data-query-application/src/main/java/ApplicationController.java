import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractEvent;
import org.hyperledger.fabric.gateway.ContractException;

import java.util.Scanner;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class ApplicationController {

    public static void applicationLoop(Contract contract) {
        ApplicationController.setContractListener(contract);
        Scanner scan = new Scanner(System.in);

        System.out.println("username: ");
        IdFactory.getInstance().setAskerName(scan.next());

        while (true) {
            System.out.println("Please select a transaction: exists, start, add, close, retrieve or remove");
            try {
                switch (scan.next()) {
                    case "exists": DataQueryTransactions.exists(contract); break;
                    case "start": DataQueryTransactions.start(contract); break;
                    case "add": DataQueryTransactions.add(contract); break;
                    case "close": DataQueryTransactions.close(contract); break;
                    case "retrieve": DataQueryTransactions.retrieve(contract); break;
                    case "remove": DataQueryTransactions.remove(contract); break;
                    case "exit": System.exit(0); break;
                    default: System.out.println("Unrecognised transaction"); break;
                }
            } catch (ContractException | InterruptedException | TimeoutException e) {
                e.printStackTrace();
            }
        }
    }

    private static void setContractListener(Contract contract) {
        Consumer<ContractEvent> consumer = contractEvent -> {
            switch(contractEvent.getName()) {
                //case "RemoveQuery":
                //    DataQuery data = DataQuery.deserialize(contractEvent.getPayload().get());
                //    ApplicationModel.getInstance().removeProcess(data.getId());
                //    break;
                default:
                    System.out.println("Event occurred: " + contractEvent.getName());
            }
        };
        contract.addContractListener(consumer);
    }
}
