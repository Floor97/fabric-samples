import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hyperledger.fabric.gateway.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.security.InvalidKeyException;
import java.security.cert.CertificateException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;
import java.io.Reader;

import org.hyperledger.fabric.gateway.Identities;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.Wallets;
import org.hyperledger.fabric.gateway.Identity;

public class Application {

    private static final Scanner scan = new Scanner(System.in);

    /**
     * Select identity from wallet
     * connect to a gateway
     * access the desired network
     * construct a transaction request for a smart contract
     * submit the transaction to the network
     * process the response
     */
    public static void main(String[] args) {
        final String CC_NAME = "aggregationprocess";
        final String CONTRACT_NAME = "aggregationprocess.pailliercontract";
        final String CHANNEL_NAME = "mychannel";

        Logger.getRootLogger().setLevel(Level.FATAL);
        InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);

        Gateway.Builder builder = Gateway.createBuilder();

        try {
            Path walletPath = Paths.get( ".", "wallet");
            Wallet wallet = Wallets.newFileSystemWallet(walletPath);

            //------INSERTING IDENTITY--------
            Path credentialPath = Paths.get("..", "..", "test-network", "organizations", "peerOrganizations", "org2.example.com", "users", "User1@org2.example.com", "msp");
            Path certificatePath = credentialPath.resolve(Paths.get("signcerts", "User1@org2.example.com-cert.pem"));
            Path privateKeyPath = credentialPath.resolve(Paths.get("keystore", "priv_sk"));
            X509Certificate certificate = null;
            PrivateKey privateKey = null;
            try {
                certificate = readX509Certificate(certificatePath);
                privateKey = getPrivateKey(privateKeyPath);
            } catch (CertificateException | InvalidKeyException e) {
                e.printStackTrace();
                return;
            }
            Identity identity = Identities.newX509Identity("Org2MSP", certificate, privateKey);

            String identityLabel = "User1@org2.example.com";
            wallet.put(identityLabel, identity);
            //------INSERTING IDENTITY--------


            String username = "User1@org2.example.com";
            Path connectionProfile = Paths.get("..", "gateway", "connection-org2.yaml");

            builder.identity(wallet, username).networkConfig(connectionProfile).discovery(true);

            try(Gateway gateway = builder.connect()) {
                Network network = gateway.getNetwork(CHANNEL_NAME);

                Contract contract = network.getContract(CC_NAME, CONTRACT_NAME);

                while(true) {
                    System.out.println("Please select a transaction: exists, start, add, close, retrieve or remove");
                    switch(scan.next()) {
                        case "exists": Application.exists(contract); break;
                        case "start": Application.start(contract); break;
                        case "add": Application.add(contract); break;
                        case "close": Application.close(contract); break;
                        case "retrieve": Application.retrieve(contract); break;
                        case "remove": Application.remove(contract); break;
                        case "exit": System.exit(0); break;
                        default:
                            System.out.println("Unrecognised transaction"); break;
                    }

                }
            }
        } catch(GatewayException | IOException | TimeoutException | InterruptedException e) {
            e.printStackTrace();
            System.exit(-1);
        }

    }

    private static void exists(Contract contract) throws ContractException, InterruptedException, TimeoutException {
        String id = scanNextLine("Transaction Exists selected\nID: ");

        byte[] responseExists = contract.submitTransaction("AggregationProcessExists", id);
        System.out.println(String.format("Asset with id %s exists: %s", id, new String(responseExists, StandardCharsets.UTF_8)));
    }

    private static void start(Contract contract) throws ContractException, InterruptedException, TimeoutException {
        String id = scanNextLine("Transaction Start selected\nID: ");
        String mod = scanNextLine("Modulus: ");
        String data = scanNextLine("Data: ");
        String exp = scanNextLine("Exponent: ");

        byte[] responseStart = contract.submitTransaction("StartAggregation", id, mod, data, exp);
        AggregationProcess aggregationProcessStart = AggregationProcess.deserialize(new String(responseStart, StandardCharsets.UTF_8));
        System.out.println("Response: " + aggregationProcessStart.toString());
    }

    private static void add(Contract contract) throws ContractException, InterruptedException, TimeoutException {
        String id = scanNextLine("Transaction Add selected\nID: ");
        String data = scanNextLine("Data: ");
        String exp = scanNextLine("Exponent: ");

        byte[] responseAdd = contract.submitTransaction("AddData", id, data, exp);
        AggregationProcess aggregationProcessAdd = AggregationProcess.deserialize(new String(responseAdd, StandardCharsets.UTF_8));
        System.out.println("Response: " + aggregationProcessAdd.toString());
    }

    private static void close(Contract contract) throws ContractException, InterruptedException, TimeoutException {
        String id = scanNextLine("Transaction Close selected\nID: ");

        contract.submitTransaction("Close", id);
        System.out.println("Transaction done");
    }

    private static void retrieve(Contract contract) throws ContractException, InterruptedException, TimeoutException {
        String id = scanNextLine("Transaction Retrieve selected\nID: ");

        byte[] responseRetrieve = contract.submitTransaction("RetrieveAggregationProcess", id);
        AggregationProcess aggregationProcessRetrieve = AggregationProcess.deserialize(new String(responseRetrieve, StandardCharsets.UTF_8));
        System.out.println("Response: " + aggregationProcessRetrieve.toString());
    }

    private static void remove(Contract contract) throws ContractException, InterruptedException, TimeoutException {
        String id = scanNextLine("Transaction Remove selected\nID: ");

        contract.submitTransaction("RemoveAggregationProcess", id);
        System.out.println("Removed process");
    }

    private static String scanNextLine(String message) {
        System.out.print(message);
        return scan.next();
    }



    private static X509Certificate readX509Certificate(final Path certificatePath) throws IOException, CertificateException {
        try (Reader certificateReader = Files.newBufferedReader(certificatePath, StandardCharsets.UTF_8)) {
            return Identities.readX509Certificate(certificateReader);
        }
    }

    private static PrivateKey getPrivateKey(final Path privateKeyPath) throws IOException, InvalidKeyException {
        try (Reader privateKeyReader = Files.newBufferedReader(privateKeyPath, StandardCharsets.UTF_8)) {
            return Identities.readPrivateKey(privateKeyReader);
        }
    }
}
