import applications.asker.DataQueryKeyStore;
import applications.asker.IdFactory;
import applications.asker.DataQueryTransactions;
import datatypes.dataquery.DataQuery;
import datatypes.values.EncryptedData;
import datatypes.values.EncryptedNonce;
import datatypes.values.EncryptedNonces;
import datatypes.values.Pair;
import encryption.NTRUEncryption;
import encryption.PaillierEncryption;
import org.bouncycastler.crypto.InvalidCipherTextException;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractEvent;
import org.hyperledger.fabric.gateway.ContractException;

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
                        Pair<String, DataQueryKeyStore> storePair = DataQueryTransactions.start(contract);
                        ApplicationModel.getInstance().addKey(storePair.getP1(), storePair.getP2());
                        break;
                    case "close":
                        DataQueryTransactions.close(contract);
                        break;
                    case "retrieve":
                        DataQueryTransactions.retrieve(contract);
                        break;
                    case "remove":
                        DataQueryTransactions.remove(contract);
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
            if(!contractEvent.getTransactionEvent().isValid()) return;
            System.out.println("Event occured: " + contractEvent.getName());
            if (!"DoneQuery".equals(contractEvent.getName())) return;
            try {
                DataQuery dataQuery = DataQuery.deserialize(contractEvent.getPayload().get());
                EncryptedData data = dataQuery.getIpfsFile().getData();
                EncryptedNonces nonces = dataQuery.getIpfsFile().getNonces();
                DataQueryKeyStore keystore = ApplicationModel.getInstance().getKey(dataQuery.getId());
                BigInteger dataAndNonces = keystore.getPaillierEncryption().decrypt(data);
                for (EncryptedNonce nonce : nonces.getNonces()) {
                    byte[] decryptedNonce = keystore.getNtruEncryption().decrypt(nonce.getNonce());
                    dataAndNonces = dataAndNonces.subtract(new BigInteger(new String(decryptedNonce)));
                }
                System.out.println("Result of " + dataQuery.getId() + " is " + dataAndNonces.toString());
            } catch (IOException e) {
                System.err.println("Could not deserialize data query asset!");
                e.printStackTrace();
            } catch(InvalidCipherTextException e) {
                System.err.println("Could not decrypt nonces with NTRUEncrypt!");
                e.printStackTrace();
            }
        };
        contract.addContractListener(consumer);
    }
}
