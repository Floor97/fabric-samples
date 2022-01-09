import applications.DataQueryKeyStore;
import applications.IdFactory;
import applications.asker.QueryTransactions;
import applications.operator.generators.NTRUEncryption;
import applications.operator.generators.PaillierEncryption;
import datatypes.dataquery.DataQuery;
import datatypes.values.EncryptedData;
import datatypes.values.EncryptedNonce;
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
            System.out.println("Please select a transaction: exists, start, add, close, retrieve or remove");
            try {
                switch (scan.next()) {
                    case "exists":
                        QueryTransactions.exists(contract);
                        break;
                    case "start":
                        Pair<String, DataQueryKeyStore> storePair = QueryTransactions.start(contract);
                        ApplicationModel.getInstance().addKey(storePair.getP1(), storePair.getP2());
                        break;
                    case "add":
                        QueryTransactions.add(contract);
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
            switch (contractEvent.getName()) {
                case "DoneQuery":
                    try {
                        DataQuery dataQuery = DataQuery.deserialize(contractEvent.getPayload().get());
                        EncryptedData data = dataQuery.getResult().getCipherData();
                        EncryptedNonces nonces = dataQuery.getResult().getCipherNonces();
                        DataQueryKeyStore keystore = ApplicationModel.getInstance().getKey(dataQuery.getId());
                        BigInteger dataAndNonces = PaillierEncryption.decrypt(data, keystore.getPaillierKeys());
                        try {
                            for (EncryptedNonce nonce : nonces.getNonces()) {
                                byte[] decryptedNonce = NTRUEncryption.decrypt(nonce.getNonce(), keystore.getPostQuantumKeys());
                                dataAndNonces = dataAndNonces.subtract(new BigInteger(new String(decryptedNonce)));
                            }
                        } catch (InvalidCipherTextException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Result of " + dataQuery.getId() + " is " + dataAndNonces.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    System.out.println("Event occurred: " + contractEvent.getName());
            }
        };
        contract.addContractListener(consumer);
    }
}
