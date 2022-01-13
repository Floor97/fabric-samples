import applications.DataQueryKeyStore;
import applications.IdFactory;
import applications.asker.QueryTransactions;
import applications.operator.generators.NTRUEncryption;
import applications.operator.generators.PaillierEncryption;
import datatypes.dataquery.DataQuery;
import datatypes.values.EncryptedData;
import datatypes.values.EncryptedNonces;
import org.bouncycastler.crypto.InvalidCipherTextException;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractEvent;
import org.hyperledger.fabric.gateway.ContractException;
import shared.Pair;

import java.math.BigInteger;
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
            System.out.println("Please select a transaction: exists, start, close, retrieve or remove. Type exit to stop.");
            try {
                switch (scan.next()) {
                    case "start":
                        Pair<String, DataQueryKeyStore> storePair = QueryTransactions.start(contract);
                        ApplicationModel.getInstance().addKey(storePair.getP1(), storePair.getP2());
                        break;
                    case "close":
                        QueryTransactions.close(contract);
                        break;
                    case "retrieve":
                        QueryTransactions.retrieve(contract);
                        break;
                    case "remove":
                        QueryTransactions.remove(contract);
                        break;
                    case "exists":
                        QueryTransactions.exists(contract);
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

    private static void setContractListener(Contract contract) {
        Consumer<ContractEvent> consumer = contractEvent -> {
            if (!"DoneQuery".equals(contractEvent.getName())) return;

            try {
                DataQuery dataQuery = DataQuery.deserialize(contractEvent.getPayload().get());
                EncryptedData data = dataQuery.getIpfsFile().getData();
                EncryptedNonces[] nonces = dataQuery.getIpfsFile().getNonces();
                DataQueryKeyStore keystore = ApplicationModel.getInstance().getKey(dataQuery.getId());
                BigInteger dataAndNonces = PaillierEncryption.decrypt(data, keystore.getPaillierKeys());

                for (EncryptedNonces nonce : nonces) {
                    byte[] decryptedNonce = NTRUEncryption.decrypt(nonce.getNonces()[0].getNonce(), keystore.getPostQuantumKeys());
                    dataAndNonces = dataAndNonces.subtract(new BigInteger(new String(decryptedNonce)));
                }
                System.out.println("Result of " + dataQuery.getId() + " is " + dataAndNonces.toString());
            } catch (InvalidCipherTextException e) {
                e.printStackTrace();
            }
        };
        contract.addContractListener(consumer);
    }
}
