import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.KeyVaultClientService;
import com.microsoft.azure.keyvault.KeyVaultConfiguration;
import com.microsoft.azure.keyvault.authentication.KeyVaultCredentials;
import com.microsoft.azure.keyvault.models.KeyOperationResult;
import com.microsoft.azure.keyvault.webkey.JsonWebKeyEncryptionAlgorithm;
import com.microsoft.windowsazure.Configuration;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.codec.binary.Base64;

public class App {

    public static void main( String[] args )
    {
        try {
            new App().start(args);
        }
        catch (Exception e) {
            System.err.println("App execution failure:" + e);
            System.exit(1);
        }
        System.exit(0);
    }

    public void start( String[] args )
	        throws InterruptedException, ExecutionException, URISyntaxException, UnsupportedEncodingException, ParseException, IOException {

        Options opts = new Options();
        opts.addOption("c", "config", true, "(Required) App config file. ex) app.conf");
        opts.addOption("t", "text", true, "(Required) Text string to process");
        BasicParser parser = new BasicParser();
        CommandLine cl;
        HelpFormatter help = new HelpFormatter();

        // parse options
        cl = parser.parse(opts, args);
        // handle server option.
        if ( !cl.hasOption("-c") || !cl.hasOption("-t") ){
            help.printHelp("App -c <app.config> -t <text-to-encrypt>", opts);
            throw new ParseException("");
        }
        String conffile = cl.getOptionValue("c");
        String textToEncrypt = cl.getOptionValue("t");

        String clientID = PropertyLoader.getInstance(conffile).getValue("ClientID");
        String clientCred = PropertyLoader.getInstance(conffile).getValue("ClientCredential");
        String keyIdentifier = PropertyLoader.getInstance(conffile).getValue("AzureKeyVaultKeyIdentifier");
	
		KeyVaultCredentials kvCred = new CustomKeyVaultCredentials(clientID, clientCred);
		Configuration config = KeyVaultConfiguration.configure(null, kvCred);
		KeyVaultClient kvc = KeyVaultClientService.create(config);

        // Encryption
		byte[] byteText = textToEncrypt.getBytes("UTF-16");
		Future<KeyOperationResult> result = kvc.encryptAsync(keyIdentifier, JsonWebKeyEncryptionAlgorithm.RSAOAEP, byteText); 
		KeyOperationResult keyoperationResult = result.get();
		System.out.println("KeyOperationResult: " + keyoperationResult);
		System.out.println("Encrypted(base64): " + Base64.encodeBase64String(keyoperationResult.getResult()));
        // Decryption
        result = kvc.decryptAsync(keyIdentifier, "RSA-OAEP", keyoperationResult.getResult());
		String decryptedResult = new String(result.get().getResult(), "UTF-16");
		System.out.println("Decpryted: " + decryptedResult );
	}
}
