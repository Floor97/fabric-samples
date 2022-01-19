import applications.asker.DataQueryKeyStore;
import applications.asker.DataQueryTransactions;
import applications.asker.IdFactory;
import datatypes.dataquery.DataQuery;
import datatypes.values.EncryptedData;
import datatypes.values.EncryptedNonce;
import datatypes.values.EncryptedNonces;
import datatypes.values.Pair;
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
        System.out.println("Operators: ");
        String operators = scan.next();
        System.out.println("Participants: ");
        String nrExpParticipants = scan.next();

        while (IdFactory.getInstance().getCounter() < 100) {
            try {
                System.out.println("Start Cycle " + IdFactory.getInstance().getCounter() + ": " + System.currentTimeMillis());
                Pair<String, DataQueryKeyStore> storePair = DataQueryTransactions.start(contract, operators, nrExpParticipants);
                ApplicationModel.getInstance().addProcess(storePair.getP1(), storePair.getP2());
                Thread.sleep(10000);
            } catch (ContractException | InterruptedException | TimeoutException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        System.out.println("Done! Press any to quit...");
        scan.next();
    }

    /**
     * The listener for the data query contract is created and set.
     *
     * @param contract the data query contract.
     */
    private static void setContractListener(Contract contract) {
        Consumer<ContractEvent> consumer = contractEvent -> {
            if (!contractEvent.getTransactionEvent().isValid() || !"DoneQuery".equals(contractEvent.getName())) return;

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

                System.out.println("End ID: " + dataQuery.getId() + ", " + System.currentTimeMillis());
            } catch (IOException e) {
                System.err.println("Could not deserialize data query asset!");
                e.printStackTrace();
            } catch (InvalidCipherTextException e) {
                System.err.println("Could not decrypt nonces with NTRUEncrypt!");
                e.printStackTrace();
            }
        };
        contract.addContractListener(consumer);
    }
}
