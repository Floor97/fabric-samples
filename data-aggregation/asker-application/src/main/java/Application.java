import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hyperledger.fabric.gateway.*;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Objects;

public class Application {

    /**
     * The identity of the client is created and the application connects to the Hyperledger
     * Fabric network using the Gateway. The channel being connected to is asker.
     *
     * @param args the args of the main method.
     */
    public static void main(String[] args) {
        Logger.getRootLogger().setLevel(Level.FATAL);
        InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);

        Gateway.Builder builder = Gateway.createBuilder();
        try {
            Path walletPath = Paths.get(".", "wallet");
            Wallet wallet = Wallets.newFileSystemWallet(walletPath);

            insertIdentity(wallet);

            String username = "User1@org2.example.com";
            Path connectionProfile = Paths.get("..", "gateway", "connection-org2.yaml");
            builder.identity(wallet, username).networkConfig(connectionProfile).discovery(true);

            try (Gateway gateway = builder.connect()) {
                Network network = gateway.getNetwork("asker");
                Contract contract = network.getContract("query", "query.eventcontract");

                ApplicationController.applicationLoop(contract);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

    }

    /**
     * The identity the client uses is created.
     *
     * @param wallet the wallet the application uses.
     */
    private static void insertIdentity(Wallet wallet) {
        Path credentialPath = Paths.get("..", "..", "test-network", "organizations", "peerOrganizations", "org2.example.com", "users", "User1@org2.example.com", "msp");
        Path certificatePath = credentialPath.resolve(Paths.get("signcerts", "cert.pem"));
        Path privateKeyPath = Objects.requireNonNull(credentialPath.resolve(Paths.get("keystore")).toFile().listFiles())[0].toPath();

        X509Certificate certificate;
        PrivateKey privateKey;
        try {
            try (Reader certificateReader = Files.newBufferedReader(certificatePath, StandardCharsets.UTF_8)) {
                certificate = Identities.readX509Certificate(certificateReader);
            }
            try (Reader privateKeyReader = Files.newBufferedReader(privateKeyPath, StandardCharsets.UTF_8)) {
                privateKey = Identities.readPrivateKey(privateKeyReader);
            }
        } catch (CertificateException | InvalidKeyException | IOException e) {
            e.printStackTrace();
            return;
        }
        Identity identity = Identities.newX509Identity("Org2MSP", certificate, privateKey);

        String identityLabel = "User1@org2.example.com";
        try {
            wallet.put(identityLabel, identity);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
