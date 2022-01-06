import dataquery.DataQuery;
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
import java.io.Reader;
import java.util.function.Consumer;

import org.hyperledger.fabric.gateway.Identities;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.Wallets;
import org.hyperledger.fabric.gateway.Identity;

public class Application {

    public static void main(String[] args) {
        Logger.getRootLogger().setLevel(Level.FATAL);
        InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);

        Gateway.Builder builder = Gateway.createBuilder();
        try {
            Path walletPath = Paths.get( ".", "wallet");
            Wallet wallet = Wallets.newFileSystemWallet(walletPath);

            insertIdentity(wallet);

            String username = "User1@org2.example.com";
            Path connectionProfile = Paths.get("..", "gateway", "connection-org2.yaml");
            builder.identity(wallet, username).networkConfig(connectionProfile).discovery(true);

            try(Gateway gateway = builder.connect()) {
                Network network = gateway.getNetwork(ApplicationModel.CHANNEL_NAME);
                Contract contract = network.getContract(ApplicationModel.CC_NAME, ApplicationModel.CONTRACT_NAME);

                ApplicationController.applicationLoop(contract);
            }
        } catch(IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

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

    private static void insertIdentity(Wallet wallet) {
        Path credentialPath = Paths.get("..", "..", "test-network", "organizations", "peerOrganizations", "org2.example.com", "users", "User1@org2.example.com", "msp");
        Path certificatePath = credentialPath.resolve(Paths.get("signcerts", "User1@org2.example.com-cert.pem"));
        Path privateKeyPath = credentialPath.resolve(Paths.get("keystore", "priv_sk"));
        X509Certificate certificate;
        PrivateKey privateKey;
        try {
            certificate = readX509Certificate(certificatePath);
            privateKey = getPrivateKey(privateKeyPath);
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
